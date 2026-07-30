# Rate Limiter — All 5 Algorithms, Traced + Commented

Common interface every algorithm below implements:

```java
public interface RateLimiter {
    boolean allowRequest(String clientId);
}
```

---

## 1. Fixed Window Counter

**Intuition:** Chop time into fixed-size buckets aligned to the clock (e.g. 10:00:00–10:00:59, 10:01:00–10:01:59...). Each client gets a counter that hard-resets to zero the instant a new bucket starts.

**Trace** (limit = 3 per 60s):
- 10:00:05 → window [10:00:00–10:00:59], count 0→1, allow
- 10:00:40 → same window, count 1→2, allow
- 10:00:55 → same window, count 2→3, allow
- 10:00:58 → same window, count already 3 → reject
- 10:01:01 → NEW window starts, count resets 0→1, allow

**The bug this creates:** a client can fire 3 requests at 10:00:58–10:00:59, then 3 more at 10:01:00–10:01:01 — 6 requests in ~3 real seconds, double the intended rate. Always raise this "boundary burst" problem when asked about this algorithm.

```java
public class FixedWindowCounterLimiter implements RateLimiter {

    private final long windowSizeMillis;      // e.g. 60_000 for a 1-minute window
    private final int maxRequestsPerWindow;   // e.g. 3

    // per-client state: which window we're currently counting, and how many requests so far
    private static class WindowState {
        long currentWindowStartMillis;
        int requestCountInCurrentWindow;
    }

    private final ConcurrentHashMap<String, WindowState> clientWindows = new ConcurrentHashMap<>();

    public FixedWindowCounterLimiter(long windowSizeMillis, int maxRequestsPerWindow) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    public boolean allowRequest(String clientId) {
        WindowState state = clientWindows.computeIfAbsent(clientId, id -> new WindowState());

        synchronized (state) {   // per-client lock, not global — never serialize unrelated clients
            long now = System.currentTimeMillis();

            // snap "now" down to its window boundary
            long windowStartForNow = (now / windowSizeMillis) * windowSizeMillis;

            if (windowStartForNow != state.currentWindowStartMillis) {
                // crossed into a new window — hard reset. This reset is the root cause of the boundary burst.
                state.currentWindowStartMillis = windowStartForNow;
                state.requestCountInCurrentWindow = 0;
            }

            if (state.requestCountInCurrentWindow < maxRequestsPerWindow) {
                state.requestCountInCurrentWindow++;
                return true;
            }
            return false;
        }
    }
}
```

**Complexity:** O(1) time, O(1) space per client.
**Use when:** cheapest possible check, boundary burst is tolerable (coarse abuse protection, not billing-grade).

---

## 2. Sliding Window Log

**Intuition:** Store the exact timestamp of every request in the trailing window, per client. On a new request, discard timestamps older than `now - windowSize`, then check how many remain.

**Trace** (limit = 3 per 60s):
- log=[] → request t=5 → nothing to prune → log=[5], count=1, allow
- request t=20 → log=[5,20], count=2, allow
- request t=45 → log=[5,20,45], count=3, allow
- request t=50 → all 3 still within last 60s → count=3, reject
- request t=70 → prune timestamps < (70-60)=10 → 5 is removed → log=[20,45], count=2, allow → log=[20,45,70]

Exact — no boundary burst — because the window is relative to *now*, not aligned to the clock.

```java
public class SlidingWindowLogLimiter implements RateLimiter {

    private final long windowSizeMillis;
    private final int maxRequestsPerWindow;

    // per-client: exact timestamp of every request still inside the trailing window
    private final ConcurrentHashMap<String, Deque<Long>> clientRequestLogs = new ConcurrentHashMap<>();

    public SlidingWindowLogLimiter(long windowSizeMillis, int maxRequestsPerWindow) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    public boolean allowRequest(String clientId) {
        Deque<Long> requestLog = clientRequestLogs.computeIfAbsent(clientId, id -> new ArrayDeque<>());

        synchronized (requestLog) {
            long now = System.currentTimeMillis();
            long windowStartBoundary = now - windowSizeMillis;

            // drop every timestamp that has aged out of the trailing window
            while (!requestLog.isEmpty() && requestLog.peekFirst() <= windowStartBoundary) {
                requestLog.pollFirst();
            }

            if (requestLog.size() < maxRequestsPerWindow) {
                requestLog.addLast(now);   // record this request's timestamp
                return true;
            }
            return false;   // still maxRequestsPerWindow timestamps within the trailing window
        }
    }
}
```

**Complexity:** O(1) amortized time (each timestamp pushed/popped once), but O(N) space per client where N = requests in the window.
**Use when:** exact correctness matters (billing-grade) and per-client volume is low enough that memory isn't a concern.

---

## 3. Sliding Window Counter (approximation)

**Intuition:** Get sliding-window-like accuracy without storing every timestamp. Keep two counters — previous fixed window's total, current fixed window's count so far — and blend them with a weight based on how much of the previous window is still "relevant."

**Formula:** `estimatedCount = previousWindowCount * overlapFraction + currentWindowCount`
where `overlapFraction` = fraction of the previous window that still falls inside the trailing lookback from now.

**Trace** (60s windows, limit=100, now is 15s into the current window):
- previous window had 80 requests total
- current window so far has 30 requests
- overlapFraction = (60-15)/60 = 0.75
- estimatedCount = 80×0.75 + 30 = 60+30 = 90 → under 100 → allow

Smooths the Fixed Window Counter's boundary burst without Sliding Window Log's memory cost. This is what most real production rate limiters (API gateways, CDNs) actually use.

```java
public class SlidingWindowCounterLimiter implements RateLimiter {

    private final long windowSizeMillis;
    private final int maxRequestsPerWindow;

    private static class DualWindowState {
        long currentWindowStartMillis;
        int previousWindowCount;   // total requests in the window just before this one
        int currentWindowCount;    // requests so far in the active window
    }

    private final ConcurrentHashMap<String, DualWindowState> clientStates = new ConcurrentHashMap<>();

    public SlidingWindowCounterLimiter(long windowSizeMillis, int maxRequestsPerWindow) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    public boolean allowRequest(String clientId) {
        DualWindowState state = clientStates.computeIfAbsent(clientId, id -> new DualWindowState());

        synchronized (state) {
            long now = System.currentTimeMillis();
            long windowStartForNow = (now / windowSizeMillis) * windowSizeMillis;

            if (windowStartForNow != state.currentWindowStartMillis) {
                // shift: "current" becomes "previous" — but ONLY if it's the immediately adjacent window.
                // if the client was idle for more than one full window, previous count must be 0, not stale data.
                boolean isAdjacentWindow = (windowStartForNow - state.currentWindowStartMillis) == windowSizeMillis;
                state.previousWindowCount = isAdjacentWindow ? state.currentWindowCount : 0;
                state.currentWindowCount = 0;
                state.currentWindowStartMillis = windowStartForNow;
            }

            double elapsedInCurrentWindow = now - state.currentWindowStartMillis;
            double overlapFraction = (windowSizeMillis - elapsedInCurrentWindow) / (double) windowSizeMillis;

            double estimatedRequestsInSlidingWindow =
                    (state.previousWindowCount * overlapFraction) + state.currentWindowCount;

            if (estimatedRequestsInSlidingWindow < maxRequestsPerWindow) {
                state.currentWindowCount++;
                return true;
            }
            return false;
        }
    }
}
```

**Complexity:** O(1) time, O(1) space per client.
**Use when:** you want sliding-window-like accuracy at fixed-window cost — the realistic production default.

---

## 4. Token Bucket

**Intuition:** Each client owns a bucket holding up to `capacity` tokens, refilling continuously at `refillRate` tokens/sec. Each request costs 1 token. Because unused tokens accumulate up to `capacity`, a client can burst instantly up to `capacity` requests, then is throttled to the steady refill rate.

**Trace** (capacity=5, refillRate=1/sec, bucket starts full):
- t=0: 5 tokens available → 5 requests arrive back-to-back → all allowed, tokens 5→0
- t=0.5: 6th request, refill adds 0.5 → 0.5 available, not enough → reject
- t=3: refill adds 3 more since last check → 3.5 available → allowed → tokens 3.5→2.5

```java
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
```

**Complexity:** O(1) time, O(1) space per client.
**Use when:** you want to allow legitimate bursts (a page load firing 10 API calls at once) while capping the long-run average. Most commonly asked algorithm, for exactly this reason.

---

## 5. Leaky Bucket

**Intuition:** Inverse of Token Bucket. Picture a bucket with a hole leaking at a constant rate; requests pour water in; overflow is rejected. Unlike Token Bucket, idle time earns nothing — output is always smoothed to exactly the leak rate, bursts are never let through even after a quiet period.

**Trace** (capacity=5, leakRate=1/sec, bucket starts empty):
- t=0: 5 requests arrive at once → water level 0→5, all 5 allowed (bucket exactly full)
- t=0.1: 6th request, almost no leak yet → level ~4.9, adding 1 more would overflow → reject
- t=5: leak has drained the bucket fully (5s × 1/s = 5) → level 0 → request allowed → level 0→1

Contrast with Token Bucket: Token Bucket rewards a quiet client with saved-up burst capacity; Leaky Bucket never converts idle time into a later burst — output is always paced at the constant leak rate.

```java
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
```

Note: production Leaky Bucket implementations often literally queue the request and process it at the leak rate (smoothing output for a downstream system) rather than rejecting outright — mention the queueing variant if asked "how would you enforce this on outbound calls."

**Complexity:** O(1) time, O(1) space per client.
**Use when:** the downstream system truly cannot handle any burst (e.g. a legacy system with fixed max throughput) — output must be perfectly smoothed, unlike Token Bucket.

---

## Quick Decision Table

| Question asked | Answer |
|---|---|
| "Cheapest, don't care about boundary burst" | Fixed Window Counter |
| "Must be byte-for-byte exact" | Sliding Window Log |
| "Accurate-ish, cheap, production-realistic" | Sliding Window Counter |
| "Allow bursts, cap long-run average" | Token Bucket |
| "Downstream can't handle ANY burst, must smooth output" | Leaky Bucket |

**The distinction interviewers probe hardest:** Token Bucket rewards idle time with later burst capacity. Leaky Bucket always outputs at a constant rate regardless of prior idle time — idle time is never "banked."
