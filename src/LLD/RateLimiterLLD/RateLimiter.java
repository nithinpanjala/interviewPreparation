package LLD.RateLimiterLLD;

public interface RateLimiter {
    boolean allowRequest(String clientId);
}
