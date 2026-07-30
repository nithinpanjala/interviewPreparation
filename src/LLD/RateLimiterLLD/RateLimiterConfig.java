package LLD.RateLimiterLLD;

public class RateLimiterConfig {
    private final long capacity;
    private final double refillTokensPerSecond;

    public RateLimiterConfig(long capacity, double refillTokensPerSecond) {
        this.capacity = capacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
    }

    public long getCapacity() { return capacity; }
    public double getRefillTokensPerSecond() { return refillTokensPerSecond; }
}
