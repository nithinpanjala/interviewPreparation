**1. Concept from zero**

A rate limiter controls how many requests a given client (user, IP, API key) can make within a time window, protecting a system from overload, abuse, or runaway retries. Analogy: a bouncer letting in only N people per minute — not because the venue can't ever hold more, but because a burst crossing some threshold degrades things for everyone inside.

Why you can't just "handle max load" instead: a single buggy client (infinite retry loop) or malicious client (DoS) can consume capacity meant for everyone else. Downstream systems you call (a third-party FX rate API, a payment gateway) often have their *own* hard rate limits — if you don't self-limit, they cut you off entirely, which is worse than throttling yourself gracefully. And request cost isn't always free — some APIs charge per call.

**2. FR — derived from tracing the request lifecycle**

A request arrives → system must decide allow/reject → decision is scoped to *some identity* (per-user, per-IP, per-API-key) against *some limit* (N requests per T seconds) → if allowed, must record that quota was consumed → if rejected, caller needs to know when to retry → quota must replenish over time, not stay exhausted forever → different clients/tiers may need different limits.

1. `allowRequest(clientId)` → true/false decision
2. Configurable limit: N requests per time window, independently per client
3. Reject over-limit requests; optionally expose retry-after time
4. Quota replenishes automatically over time (no manual reset)
5. Pluggable algorithm — fixed window, sliding window, token bucket, leaky bucket — swappable without touching caller code
6. Independent state per client key — one client's usage never affects another's

**NFR — derived from concurrency / failure / resource-limit lenses**

1. **Thread-safety** — this sits in the hot path, hit by many threads concurrently for the *same* client (e.g., retries) and *different* clients simultaneously
2. **Low latency** — the check itself must be near O(1); it's overhead added to every single request in the system
3. **Bounded memory** — one bucket per client key; with millions of clients, stale entries must be evicted, not kept forever
4. **Precision vs cost tradeoff** — fixed window is cheap but bursts at window boundaries; sliding window is accurate but costlier — pick per interviewer's follow-up
5. **Distributed consistency** — if the service runs on multiple nodes, in-memory state alone under-counts; needs shared state (Redis) or accepted eventual consistency
6. **Fail-open vs fail-closed** — if the backing store (Redis) is down, decide: let requests through (fail-open, risk overload) or block everything (fail-closed, risk full outage)

**3. Design**

*Entities*
- `RateLimiter` (interface) — `allowRequest(clientId)`
- `RateLimiterConfig` — capacity, refillTokensPerSecond
- `TokenBucket` — per-client state: availableTokens, lastRefillTimestamp, capacity, refillRate; owns `tryConsume()`
- `TokenBucketRateLimiter implements RateLimiter` — `ConcurrentHashMap<clientId, TokenBucket>`

*Patterns*
- **Strategy** — `RateLimiter` interface lets you swap Token Bucket for Sliding Window without touching caller code
- **Factory** — builds the right `RateLimiterConfig`/limiter per client tier (free vs paid)
- Not a Decorator/Proxy problem this time — no "intercept and redirect" trick like Connection Pool's `close()`. Don't force that pattern in here.

*Algorithm comparison (know this table cold — near-guaranteed follow-up)*

| Algorithm | Burst handling | Memory | Accuracy |
|---|---|---|---|
| Fixed Window Counter | Bursts at window boundary (2x limit possible) | O(1) per client | Low |
| Sliding Window Log | No boundary burst | O(N) — stores every timestamp | Exact |
| Sliding Window Counter | Approximates sliding, smooths boundary | O(1) | High, approximate |
| Token Bucket | Allows controlled bursts up to capacity | O(1) | High |
| Leaky Bucket | Smooths output to constant rate, no bursts | O(1) | High, no burst allowed |

Token Bucket is implemented below — it's the most commonly asked because it elegantly allows *controlled* bursts (unused capacity) while still enforcing a long-run rate.

*Code*

```java
public interface RateLimiter {
    boolean allowRequest(String clientId);
}

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
```

```java
public class RateLimiterDemo {
    public static void main(String[] args) {
        RateLimiterConfig config = new RateLimiterConfig(5, 1.0); // burst of 5, refill 1/sec
        RateLimiter limiter = new TokenBucketRateLimiter(config);

        for (int i = 0; i < 7; i++) {
            System.out.println("client-A request " + i + " allowed=" + limiter.allowRequest("client-A"));
        }
    }
}
```

*Thread safety — where and why*
- `ConcurrentHashMap.computeIfAbsent` — atomic get-or-create per client key. Without it, two concurrent first-requests for the same new client could each construct a separate `TokenBucket`, silently doubling that client's effective quota.
- `synchronized (bucketLock)` **per bucket**, not a global lock across all clients — this is the key design decision. A global lock would serialize every request in the system through one mutex regardless of client, killing throughput. Locking per-bucket means client A and client B's requests never contend.
- Lazy refill (compute elapsed time on each `tryConsume()` call) instead of a background ticking thread per bucket — avoids spinning up threads per client, which wouldn't scale to millions of clients.

*Extensibility*
- Per-tier limits: factory returns different `RateLimiterConfig` per client tier (free vs paid) from a lookup, same `TokenBucketRateLimiter` code untouched.
- Swap algorithm: implement `RateLimiter` with a `SlidingWindowRateLimiter`, inject wherever the interface is used — no caller changes.
- Stale bucket eviction: track `lastAccessTimestamp` on `TokenBucket`, background sweep removes buckets untouched for N minutes — bounds memory in NFR #3.
- Distributed version: move bucket state into Redis, use a Lua script for atomic check-and-decrement across nodes (this is where your Redis/distributed-caching experience is a direct talking point).

*Interview walkthrough script*
1. "Requirements: per-client limit, reject over quota, replenish automatically, pluggable algorithm."
2. "I'll implement Token Bucket — it allows controlled bursts up to capacity while enforcing a long-run rate, which fixed-window can't do without boundary issues."
3. "Concurrency: per-client lock, not global — that's the throughput-critical decision — plus `computeIfAbsent` for atomic bucket creation."
4. "Refill is lazy, computed from elapsed time on each request, not a ticking background thread — scales to millions of clients."
5. "In production/distributed, I'd move this to Redis with a Lua script so the check-and-decrement is atomic across nodes."

*Connect to your experience*

Directly maps to protecting your FX Rate Service or transaction platform from upstream abuse, and to self-limiting outbound calls to third-party FX providers who enforce their own caps. The distributed extension (Redis + Lua) is a straight line from your caching-layer work at TechMojo — same tool, different use: shared atomic counters instead of a cache.

Your turn — explain the design back to me, or paste code cold. Which one?