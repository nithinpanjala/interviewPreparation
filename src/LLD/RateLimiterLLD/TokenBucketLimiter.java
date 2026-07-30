package LLD.RateLimiterLLD;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketLimiter implements RateLimiter {

    private final long bucketCapacity;          // max tokens a bucket can hold = max burst size
    private final double refillTokensPerSecond; // steady-state allowed rate

    private static class Bucket {
        double tokensAvailable;
        long lastRefillTimestampNanos;
    }

    private final ConcurrentHashMap<String, Bucket> clientBuckets = new ConcurrentHashMap<>();

    public TokenBucketLimiter(long bucketCapacity, double refillTokensPerSecond) {
        this.bucketCapacity = bucketCapacity;
        this.refillTokensPerSecond = refillTokensPerSecond;
    }

    @Override
    public boolean allowRequest(String clientId) {
        Bucket bucket = clientBuckets.computeIfAbsent(clientId, id -> {
            Bucket newBucket = new Bucket();
            newBucket.tokensAvailable = bucketCapacity;       // start full: first burst is always allowed
            newBucket.lastRefillTimestampNanos = System.nanoTime();
            return newBucket;
        });

        synchronized (bucket) {
            refill(bucket);   // top up tokens based on elapsed time since we last looked
            if (bucket.tokensAvailable >= 1.0) {
                bucket.tokensAvailable -= 1.0;   // spend one token for this request
                return true;
            }
            return false;     // not enough tokens — client is over their rate
        }
    }

    // lazy refill: instead of a ticking background thread per client, compute how many tokens
    // "should have" accumulated since we last touched this bucket
    private void refill(Bucket bucket) {
        long now = System.nanoTime();
        long elapsedNanos = now - bucket.lastRefillTimestampNanos;
        double tokensEarnedSinceLastCheck = (elapsedNanos / 1_000_000_000.0) * refillTokensPerSecond;

        if (tokensEarnedSinceLastCheck > 0) {
            bucket.tokensAvailable = Math.min(bucketCapacity, bucket.tokensAvailable + tokensEarnedSinceLastCheck);
            bucket.lastRefillTimestampNanos = now;
        }
    }
}