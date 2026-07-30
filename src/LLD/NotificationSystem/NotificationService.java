package LLD.NotificationSystem;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

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
    public void notify(NotificationRequest request) {
        if (!processedNotificationIds.add(request.getNotificationId())) {
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
                    System.err.println("Delivery failed on " + channelType + " for notification " + request.getNotificationId() + ": " + e.getMessage());
                }
            });
        }
    }

    public void shutdown() {
    }
}
