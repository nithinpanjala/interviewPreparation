package LLD.RateLimiterLLD;

public class TokenBucket {
    private final long capacity;
    private final double refillTokensPerSecond;
    private double availableTokens;
    private long lastRefillTimestampNanos;
    private final Object bucketLock = new Object();   // fine-grained: one lock PER bucket, not global

    public TokenBucket(RateLimiterConfig config) {
        this.capacity = config.getCapacity();
        this.refillTokensPerSecond = config.getRefillTokensPerSecond();
        this.availableTokens = capacity;               // start full — first burst is allowed
        this.lastRefillTimestampNanos = System.nanoTime();
    }

    public boolean tryConsume() {
        synchronized (bucketLock) {
            refill();
            if (availableTokens >= 1.0) {
                availableTokens -= 1.0;
                return true;
            }
            return false;
        }
    }

    private void refill() {
        long now = System.nanoTime();
        long elapsedNanos = now - lastRefillTimestampNanos;
        double tokensToAdd = (elapsedNanos / 1_000_000_000.0) * refillTokensPerSecond;
        if (tokensToAdd > 0) {
            availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
            lastRefillTimestampNanos = now;
        }
    }
}
