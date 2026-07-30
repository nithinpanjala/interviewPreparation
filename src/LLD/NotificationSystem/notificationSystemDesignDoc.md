Progress: Connection Pool, Rate Limiter, Elevator System, Splitwise — done. Next: **Notification System** — this one maps almost exactly onto your resume strengths (Kafka, ExecutorService, idempotent processing), so it's worth full attention.

**1. Concept from zero**

Some event happens in the system (order confirmed, password reset, payment failed) and one or more users need to be told, potentially via multiple channels — email, SMS, push, in-app. The system needs to decouple *what triggered the notification* from *how it gets delivered*, respect each user's channel preferences, format the message differently per channel (SMS has a character limit, email doesn't), and survive a channel provider being temporarily down without blocking everything else.

Why it's not "just call sendEmail()": if sending is done synchronously inline with business logic, a slow SMS gateway makes your order-confirmation API slow too — the business logic shouldn't wait on notification delivery at all. And a naive implementation that fails all channels because one provider is down is a real production outage pattern; channels must fail independently.

**2. FR — derived from tracing the event lifecycle**

An event occurs → system determines which users should be notified and via which channels (their stored preferences) → for each channel, render appropriate content (channel-specific template) → attempt delivery via that channel's provider → on failure, retry a bounded number of times → the triggering business logic returns immediately, without waiting for any of this.

1. `notify(recipient, eventType, templateData)` — trigger notification dispatch
2. Support multiple channels — Email, SMS, Push, In-App — pluggable, add new ones without touching existing code
3. Per-event, per-channel message templating
4. Respect per-user channel preferences (opted in/out)
5. Retry failed channel deliveries, bounded attempts
6. Asynchronous — caller never blocks on delivery

**NFR — derived from concurrency/failure/resource-limit lenses**

1. **Fault isolation** — one channel's provider being down must never block or fail another channel's delivery
2. **Thread-safety** — many events firing concurrently across the system, dispatching in parallel
3. **Idempotency** — retries, or duplicate event delivery (e.g. an at-least-once message queue redelivering), must never cause a duplicate send to the user
4. **Extensibility** — new channel = new class, zero changes to orchestration logic
5. **Non-blocking** — dispatch must never make the triggering business transaction slower

**3. Design**

*Entities*
- `NotificationChannel` (interface) — `send(recipient, renderedMessage)`
- `EmailChannel`, `SmsChannel`, `PushChannel` — implementations
- `TemplateEngine` (interface) — renders content per event type + channel
- `UserPreferenceStore` (interface) — which channels a user has enabled
- `RetryingNotificationChannel` — wraps any `NotificationChannel`, adds retry/backoff transparently
- `NotificationService` — orchestrator: resolves preferences, renders, dispatches async per channel

*Patterns*
- **Strategy** — `NotificationChannel` per delivery mechanism, selected per user's enabled set
- **Decorator** — `RetryingNotificationChannel` wraps a channel and intercepts `send()` to add retry, without the channel or the caller knowing. Same shape as Connection Pool's `PooledConnection` intercepting `close()` — recognize this pattern-shape on sight, it keeps showing up
- **Pub-Sub** (production extension) — event producers would publish to a message bus rather than call `notify()` directly, fully decoupling producer from delivery — this is where your Kafka experience is a direct talking point

*Code*

```java
public enum ChannelType { EMAIL, SMS, PUSH, IN_APP }

public class NotificationRequest {
    private final User recipient;
    private final String eventType;                 // e.g. "ORDER_CONFIRMED"
    private final Map<String, String> templateData;  // values to substitute into the template

    public NotificationRequest(User recipient, String eventType, Map<String, String> templateData) {
        this.recipient = recipient;
        this.eventType = eventType;
        this.templateData = templateData;
    }

    public User getRecipient() { return recipient; }
    public String getEventType() { return eventType; }
    public Map<String, String> getTemplateData() { return templateData; }
}

public class NotificationDeliveryException extends Exception {
    public NotificationDeliveryException(String message) { super(message); }
    public NotificationDeliveryException(String message, Throwable cause) { super(message, cause); }
}

public interface NotificationChannel {
    ChannelType getChannelType();
    void send(User recipient, String renderedMessage) throws NotificationDeliveryException;
}

public interface TemplateEngine {
    String render(String eventType, ChannelType channelType, Map<String, String> templateData);
}

public interface UserPreferenceStore {
    Set<ChannelType> getEnabledChannels(User user);
}
```

```java
// Decorator — adds retry to ANY NotificationChannel without changing that channel's own code
public class RetryingNotificationChannel implements NotificationChannel {
    private final NotificationChannel delegate;
    private final int maxAttempts;
    private final long baseRetryDelayMillis;

    public RetryingNotificationChannel(NotificationChannel delegate, int maxAttempts, long baseRetryDelayMillis) {
        this.delegate = delegate;
        this.maxAttempts = maxAttempts;
        this.baseRetryDelayMillis = baseRetryDelayMillis;
    }

    @Override
    public ChannelType getChannelType() { return delegate.getChannelType(); }

    @Override
    public void send(User recipient, String renderedMessage) throws NotificationDeliveryException {
        NotificationDeliveryException lastFailure = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                delegate.send(recipient, renderedMessage);
                return;   // success — stop retrying
            } catch (NotificationDeliveryException e) {
                lastFailure = e;
                sleepBeforeRetry(attempt);   // linear backoff: wait longer after each failed attempt
            }
        }
        throw new NotificationDeliveryException("Failed after " + maxAttempts + " attempts", lastFailure);
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(baseRetryDelayMillis * attempt);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
```

```java
public class NotificationService {
    private final Map<ChannelType, NotificationChannel> channels;
    private final TemplateEngine templateEngine;
    private final UserPreferenceStore preferenceStore;
    private final ExecutorService dispatchExecutor;
    private final Set<String> processedNotificationIds = ConcurrentHashMap.newKeySet();  // idempotency guard

    public NotificationService(Map<ChannelType, NotificationChannel> channels,
                                TemplateEngine templateEngine,
                                UserPreferenceStore preferenceStore,
                                ExecutorService dispatchExecutor) {
        this.channels = channels;
        this.templateEngine = templateEngine;
        this.preferenceStore = preferenceStore;
        this.dispatchExecutor = dispatchExecutor;
    }

    // called by business logic — returns immediately, actual sends happen on background threads
    public void notify(String notificationId, NotificationRequest request) {
        if (!processedNotificationIds.add(notificationId)) {
            return;   // already handled this exact notification — guards against duplicate event delivery
        }

        Set<ChannelType> enabledChannels = preferenceStore.getEnabledChannels(request.getRecipient());

        for (ChannelType channelType : enabledChannels) {
            NotificationChannel channel = channels.get(channelType);
            if (channel == null) continue;   // channel not configured — skip, don't fail the whole request

            // each channel dispatched as an INDEPENDENT task — one channel's failure/slowness
            // never blocks or delays another channel's delivery
            dispatchExecutor.submit(() -> {
                String renderedMessage = templateEngine.render(request.getEventType(), channelType, request.getTemplateData());
                try {
                    channel.send(request.getRecipient(), renderedMessage);
                } catch (NotificationDeliveryException e) {
                    System.err.println("Delivery failed on " + channelType + " for notification " + notificationId + ": " + e.getMessage());
                }
            });
        }
    }
}
```

*Thread safety — where and why*

`ExecutorService.submit()` per channel per request — this is the deliberate design choice, and it's a direct callback to your resume strength: independent async tasks mean channel isolation is structural, not just a try-catch — Email failing can never delay or block SMS. `ConcurrentHashMap.newKeySet()` for `processedNotificationIds` — the dedup check-and-add (`.add()` returns false if already present) must be atomic under concurrent retries or duplicate deliveries; this is the same idempotency pattern as your wallet-transfer/ACID transaction work. `Map<ChannelType, NotificationChannel>` backed by `ConcurrentHashMap` if channels are ever registered/deregistered at runtime.

*Extensibility*
- New channel (WhatsApp, Slack) → implement `NotificationChannel`, register in the map — `NotificationService` untouched
- Swap retry policy → extract a `BackoffStrategy` interface, inject into `RetryingNotificationChannel` — linear vs exponential backoff becomes pluggable too
- Production event decoupling → producers publish events to Kafka; a consumer calls `notificationService.notify()` — this fully removes the producer's dependency on the notification system being available at all, and gives you replay/redelivery for free, which is exactly why the idempotency guard above is a hard requirement, not a nice-to-have

*Interview walkthrough script*
1. "Channel abstraction is Strategy — one `NotificationChannel` per delivery mechanism, selected from the user's stored preferences."
2. "Retry is a Decorator — wraps any channel, adds backoff transparently, same shape as a proxy intercepting a method call."
3. "Dispatch is async via `ExecutorService` — each channel send is an independent task, so failures are isolated per channel, not per request."
4. "Idempotency guard via a concurrent dedup set — necessary the moment you have retries or an at-least-once event source upstream."
5. "In production, I'd put Kafka between event producers and this service — full decoupling, plus replay if the notification service was briefly down."

*Connect to your experience*

This is the closest match to your actual stack: the async per-channel dispatch is your ExecutorService/multithreading strength directly applied; the idempotency dedup set is the same discipline as your wallet-transfer ACID work; the production Kafka extension is literally your event-driven microservices experience. If the interviewer asks "how would this actually be wired at scale," lead with Kafka — it's your strongest card in this specific problem.

