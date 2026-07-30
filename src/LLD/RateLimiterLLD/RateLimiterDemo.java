package LLD.RateLimiterLLD;

public class RateLimiterDemo {
    public static void main(String[] args) {
        RateLimiterConfig config = new RateLimiterConfig(10, 0.8); // burst of 5, refill 1/sec
        RateLimiter tokenBucketRateLimiter = new TokenBucketRateLimiter(config);
        RateLimiter fixedWindowCounterLimiter = new FixedWindowCounterLimiter(10,5);
        RateLimiter slidingWindowCounterLimiter = new SlidingWindowCounterLimiter(10,5);
        RateLimiter slidingWindowLogLimiter = new SlidingWindowLogLimiter(10,5);
        RateLimiter leakyBucketLimiter = new LeakyBucketLimiter(10,5);

        for (int i = 0; i < 15; i++) {
            System.out.println("fixedWindowCounterLimiter client-A request " + i + " allowed=" + fixedWindowCounterLimiter.allowRequest("client-A"));
        }


        for (int i = 0; i < 15; i++) {
            System.out.println("slidingWindowCounterLimiter client-A request " + i + " allowed=" + slidingWindowCounterLimiter.allowRequest("client-A"));
        }

        for (int i = 0; i < 15; i++) {
            System.out.println("slidingWindowLogLimiter client-A request " + i + " allowed=" + slidingWindowLogLimiter.allowRequest("client-A"));
        }

        for (int i = 0; i < 15; i++) {
            System.out.println("tokenBucketRateLimiter client-A request " + i + " allowed=" + tokenBucketRateLimiter.allowRequest("client-A"));
        }

        for (int i = 0; i < 15; i++) {
            System.out.println("leakyBucketLimiter client-A request " + i + " allowed=" + leakyBucketLimiter.allowRequest("client-A"));
        }
    }
}
