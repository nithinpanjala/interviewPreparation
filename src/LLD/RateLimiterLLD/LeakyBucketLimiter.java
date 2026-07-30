package LLD.RateLimiterLLD;

import java.util.concurrent.ConcurrentHashMap;

public class LeakyBucketLimiter implements RateLimiter {

    private final long bucketCapacity;      // max "water" the bucket can hold before overflowing
    private final double leakRatePerSecond; // constant rate requests are allowed through

    private static class Bucket {
        double currentWaterLevel;
        long lastLeakTimestampNanos;
    }

    private final ConcurrentHashMap<String, Bucket> clientBuckets = new ConcurrentHashMap<>();

    public LeakyBucketLimiter(long bucketCapacity, double leakRatePerSecond) {
        this.bucketCapacity = bucketCapacity;
        this.leakRatePerSecond = leakRatePerSecond;
    }

    @Override
    public boolean allowRequest(String clientId) {
        Bucket bucket = clientBuckets.computeIfAbsent(clientId, id -> {
            Bucket newBucket = new Bucket();
            newBucket.currentWaterLevel = 0;                  // starts empty — no free burst like Token Bucket
            newBucket.lastLeakTimestampNanos = System.nanoTime();
            return newBucket;
        });

        synchronized (bucket) {
            leak(bucket);   // drain water based on elapsed time since we last looked
            if (bucket.currentWaterLevel + 1.0 <= bucketCapacity) {
                bucket.currentWaterLevel += 1.0;   // this request adds one unit of "water"
                return true;
            }
            return false;   // bucket would overflow — reject (this variant discards, doesn't queue)
        }
    }

    // lazy leak: compute how much would have drained since we last checked, same trick as Token Bucket's refill
    private void leak(Bucket bucket) {
        long now = System.nanoTime();
        long elapsedNanos = now - bucket.lastLeakTimestampNanos;
        double leakedSinceLastCheck = (elapsedNanos / 1_000_000_000.0) * leakRatePerSecond;

        if (leakedSinceLastCheck > 0) {
            bucket.currentWaterLevel = Math.max(0, bucket.currentWaterLevel - leakedSinceLastCheck);
            bucket.lastLeakTimestampNanos = now;
        }
    }
}