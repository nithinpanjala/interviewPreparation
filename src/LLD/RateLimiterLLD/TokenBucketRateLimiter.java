package LLD.RateLimiterLLD;

import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketRateLimiter implements RateLimiter {
    private final ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();
    private final RateLimiterConfig config;

    public TokenBucketRateLimiter(RateLimiterConfig config) {
        this.config = config;
    }

    @Override
    public boolean allowRequest(String clientId) {
        TokenBucket bucket = clientBuckets.computeIfAbsent(clientId, id -> new TokenBucket(config));
        return bucket.tryConsume();
    }
}