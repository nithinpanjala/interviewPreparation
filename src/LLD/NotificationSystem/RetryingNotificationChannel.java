package LLD.NotificationSystem;

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
    public ChannelType getChannelType() {
        return delegate.getChannelType();
    }

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
