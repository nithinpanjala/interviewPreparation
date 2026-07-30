Cross-referencing all four lists by frequency, source credibility (cited Reddit/Glassdoor/LeetCode Discuss reports weighted higher than uncited generic lists), and pattern overlap. Here's the compiled top 15, ranked by how many independent sources flagged it as high-probability for Salesforce SMTS:

**Tier A — Must-do (hit 3+ sources, including cited ones as Tier-1)**

| # | Problem | Why it's high-confidence |
|---|---|---|
| 1 | Connection Pool (thread-safe) | Tier-1 in both cited sources + the synthesis list. Most repeated single problem. |
| 2 | Stock Trading / Order Matching Engine | Tier-1 in both cited sources, named explicitly for Hyderabad SMTS rounds (Feb 2026). |
| 3 | LRU/LFU Cache (built from scratch, thread-safe) | Appears in all four lists. |
| 4 | Job/Task Scheduler (multithreaded, periodic + dependent tasks) | Tier-1/Tier-2 in both cited sources + synthesis list. |
| 5 | Rate Limiter (window-based, distributed variant) | Cited as Tier-1 (Dec 2025, Feb 2026 SMTS rounds specifically). |
| 6 | Elevator System (multi-lift, state pattern) | Cited Tier-1 (Dec 2025 SMTS) + appears everywhere else. |
| 7 | Parking Lot (multi-floor, concurrent allocation) | In all four; tracked as single most common Salesforce LLD prompt by a dedicated tracker. |
| 8 | Notification System (multi-channel, fallback/rate-limit) | Appears in 3/4 lists; also matches your own existing prep plan. |

**Tier B — Strong secondary (worth the time if Tier A is solid)**

| # | Problem | Why |
|---|---|---|
| 9 | Booking/Reservation System (meeting room or ticket booking — same core pattern: slot locking, double-booking prevention) | Cited specifically for Hyderabad (Meeting Scheduler, Nov 2025 onsite) + shows up as Ticketing/Cab Booking elsewhere. |
| 10 | Message Queue / Pub-Sub (mini Kafka: producer-consumer, ack, retry) | Tier-1 in one cited source; also directly in your own LLD schedule. |
| 11 | Splitwise (expense-sharing/ledger) | Recurs in two lists as a common Salesforce-adjacent favorite. |
| 12 | Shopping Cart / Promotion Engine (strategy+composite for discount stacking) | In two lists, tests pattern usage over brute-force logic. |

**Tier C — Fill-in if time remains**

| # | Problem | Why |
|---|---|---|
| 13 | Vending Machine or ATM (state machine basics) | Common fallback generic OOD question, quick to prep. |
| 14 | Key-Value Store with TTL (put/get/delete/expiry) | Specifically named in Glassdoor Hyderabad reports. |
| 15 | Snake & Ladder or Chess (turn-based game engine) | Lower priority per cited sources (Tier-3) but still a recurring "fast OOD translation under time pressure" test. |

**Prep order given limited time:** lock down 1–8 cold (these cover every core pattern: concurrency/locking, state machine, strategy/factory, matching/event-driven). If any time is left, do 9–12. Only touch 13–15 if 1–12 are solid — they're lower-signal across sources.

Note: problem 9 covers three of your sources' "booking" variants at once — the underlying design (slot reservation + concurrency) is nearly identical whether it's a meeting room, movie ticket, or cab booking, so one deep prep covers all three framings.

Given the time crunch, here it is directly — no file wrangling.

**Cross-cutting rules (say these out loud — they cover most follow-ups):**

Lock scope: per-key lock when each key's state is independent (Rate Limiter buckets, Elevators, Meeting Rooms). Global lock when operations mutate shared structure any key can affect (LRU/LFU ordering, Splitwise's multi-user balance update, Connection Pool's release-vs-shutdown). Proxy/Decorator = wrap the same interface, intercept ONE method transparently (Connection Pool's `close()`, Notification's `send()`) — the Factory is never the proxy. Strategy is the most reused pattern here (Rate Limiter's 5 algorithms, Elevator's 3 dispatch policies, Splitwise's 3 split types, Parking Lot's spot assignment). Money is always `BigDecimal`, handle the rounding remainder explicitly. "Check-then-act" is the recurring race in booking/allocation problems (Meeting Room, Parking Lot) — check and reserve must be one atomic step. A `get()` that mutates ordering isn't a read — `ReadWriteLock` won't help (LRU/LFU).

**1. Connection Pool** — bounded reusable connections, borrow with timeout. Object Pool + Factory + Proxy (`PooledConnection` intercepts `close()`). `BlockingQueue`+`Set`+`Semaphore`. `ReentrantLock` for release-vs-shutdown atomicity, `returned` flag guards double-release. Gotcha: Factory ≠ Proxy.

**2. Rate Limiter** — 5 algorithms behind one Strategy interface. Fixed Window (counter+timestamp), Sliding Log (`Deque`), Sliding Counter (two counters blended), Token Bucket (lazy refill), Leaky Bucket (lazy leak). Per-client lock, `computeIfAbsent` for atomic bucket creation. Gotcha: Token Bucket and Leaky Bucket (meter variant) are mathematically dual — `tokens + water = capacity` always.

**3. Elevator** — hall calls + car calls, LOOK/SCAN algorithm. Strategy for dispatch (Nearest/Least-Busy/Zone). Two `NavigableSet` per elevator for O(log n) nearest-stop. Per-elevator lock. Gotcha: reverse only when nothing left ahead; cross-elevator dispatch race is an accepted simplification.

**4. Parking Lot** — multi-floor spot allocation. Singleton + Factory + Strategy. Per-floor spot collections by type. Atomic check-and-reserve per spot. Gotcha: same check-then-act race as Meeting Room.

**5. Notification System** — multi-channel dispatch, retry, dedup. Strategy (channels) + Decorator (`RetryingNotificationChannel`) + Pub-Sub (Kafka extension). `Map<ChannelType,...>` + concurrent dedup `Set`. `ExecutorService` per-channel task for fault isolation. Gotcha: failures swallowed inside the async task, never reach the caller of `notify()`.

**6. Splitwise** — equal/exact/percentage splits, net balances. Strategy (`SplitStrategy`). Nested `Map<user,Map<user,BigDecimal>>`. Single global lock (one expense touches many users atomically). Gotcha: `BigDecimal`, deterministic rounding-remainder handling.

**7. Job Scheduler** — time-based + dependency-based (DAG). `Delayed`/`DelayQueue` for time; incremental Kahn's-algorithm topological sort for dependencies. `AtomicInteger.decrementAndGet()` for exactly-once diamond-dependency dispatch; lock around cycle-check. Gotcha: reschedule periodic tasks AFTER execution (fixed-delay), not before.

**8. Meeting Room** — atomic booking, search by criteria. Per-room `TreeMap<startTime,Booking>` — only floor/ceiling neighbor can conflict, O(log n). Per-room lock, check-and-reserve in one block. Gotcha: search results are a stale snapshot; real safety is in `tryBook`, not search.

**9. LRU Cache** — O(1) get/put, evict oldest. `HashMap<key,Node>` + DLL with sentinels. Single global lock. Gotcha: map points to the NODE, not the value — that's the O(1) trick.

**10. LFU Cache** — O(1) get/put, evict least-frequent, LRU tie-break. `HashMap<key,Node>` + `Map<freq,LinkedHashSet<key>>` + `minFrequency`. Single global lock. Gotcha: `LinkedHashSet` gives the tie-break free; `minFrequency` updates incrementally, never scans.




# LLD Interview Memory Sheet — Last-Hour Recall

## Cross-cutting rules (say these out loud when relevant — they cover most follow-ups)

- **Lock scope decision:** per-key/per-resource lock when each key's state is fully independent (Rate Limiter buckets, Elevators, Meeting Rooms). Global lock when operations mutate *shared* structure that any key's access can affect (LRU/LFU's shared ordering, Splitwise's multi-user balance update, Connection Pool's release-vs-shutdown atomicity).
- **Proxy/Decorator shape:** wrap the same interface, intercept ONE method transparently to redirect behavior. Seen in: Connection Pool (`close()` → release), Notification System (`send()` → retry). The Factory is never the proxy — don't mix these up.
- **Strategy pattern:** the single most reused pattern across this list — Rate Limiter (5 algorithms), Elevator (3 dispatch policies), Splitwise (3 split types), Parking Lot (spot assignment). Swap implementation, zero change to caller.
- **Money is always `BigDecimal`**, never float/double. Any split/divide must explicitly handle the rounding remainder.
- **"Check-then-act" race** is the recurring concurrency bug across booking/allocation problems (Meeting Room, Parking Lot) — checking availability and reserving must be ONE atomic step, never two.
- **Read methods that mutate are not reads** — `get()` on LRU/LFU updates ordering as a side effect, so `ReadWriteLock` doesn't help; every op needs exclusive access.

---

## 1. Connection Pool (thread-safe)
- **Core ask:** bounded pool of reusable expensive connections; borrow with timeout, return transparently
- **Pattern(s):** Object Pool (overall), Factory (creates raw connections), Proxy/Decorator (`PooledConnection` intercepts `close()`)
- **Unique data structure:** `BlockingQueue` (free) + `Set` (active) + `Semaphore(maxSize)` (bounds count)
- **Concurrency move:** Semaphore for count, `ReentrantLock` for atomic release-vs-shutdown, `returned` flag guards double-release
- **Gotcha:** Factory is NOT a proxy — only `PooledConnection` is. Double-release corrupts state if unguarded.

## 2. Rate Limiter (5 algorithms)
- **Core ask:** allow/reject requests per client per window
- **Pattern(s):** Strategy — 5 interchangeable algorithms behind one interface
- **Unique data structure:** Fixed Window (counter+timestamp) / Sliding Log (`Deque` of timestamps) / Sliding Counter (two counters, weighted blend) / Token Bucket (double, lazy refill) / Leaky Bucket (double, lazy leak)
- **Concurrency move:** per-client lock, `ConcurrentHashMap.computeIfAbsent` for atomic bucket creation
- **Gotcha:** Token Bucket and Leaky Bucket (meter variant) are mathematically dual — `tokensAvailable + waterLevel = capacity`, always. Real Leaky Bucket (queue+server) *shapes* output; the meter variant only *polices* it like Token Bucket.

## 3. Elevator System
- **Core ask:** hall calls + car calls, N elevators, serve efficiently
- **Pattern(s):** Strategy — dispatch (Nearest / Least-Busy / Zone-Based, composable)
- **Unique data structure:** two `NavigableSet` per elevator (`upStops`, `downStops`) — LOOK/SCAN algorithm, `first()`/`last()` give nearest stop in O(log n)
- **Concurrency move:** per-elevator lock (elevators are independent)
- **Gotcha:** reverse direction only when nothing left ahead. Cross-elevator dispatch has an inherent TOCTOU race — acceptable, name it proactively.

## 4. Parking Lot
- **Core ask:** multi-floor, multiple spot types, allocate/free a spot for a vehicle
- **Pattern(s):** Singleton (lot instance), Factory (ticket/spot creation), Strategy (spot assignment policy)
- **Unique data structure:** per-floor spot collections indexed by spot type for fast matching lookup
- **Concurrency move:** atomic check-and-reserve per spot, same shape as Meeting Room
- **Gotcha:** two vehicles racing for the same spot is the check-then-act bug — allocation must be one atomic step.

## 5. Notification System
- **Core ask:** multi-channel dispatch per user preference, with retry and dedup
- **Pattern(s):** Strategy (channel selection), Decorator (`RetryingNotificationChannel` wraps `send()`), Pub-Sub (production extension via Kafka)
- **Unique data structure:** `Map<ChannelType, NotificationChannel>`, `Set` (concurrent) for idempotency dedup
- **Concurrency move:** `ExecutorService` — one async task per channel per request (fault isolation), `ConcurrentHashMap.newKeySet()` dedup guard
- **Gotcha:** exceptions from a failed channel are swallowed INSIDE the async task — never propagate to the caller of `notify()`. That's what makes the decoupling real, not just "it's on a background thread."

## 6. Splitwise
- **Core ask:** split expenses (equal/exact/percentage), track net balances, settle up
- **Pattern(s):** Strategy (`SplitStrategy`: Equal / Exact / Percentage)
- **Unique data structure:** nested `Map<userId, Map<userId, BigDecimal>>` net balance ledger
- **Concurrency move:** single global lock — one expense mutates many users' balances at once, must be atomic together
- **Gotcha:** `BigDecimal` always. Equal split must deterministically absorb the rounding remainder (give leftover cents to one fixed participant) or money silently vanishes.

## 7. Job/Task Scheduler
- **Core ask:** time-based (once/periodic) + dependency-based (DAG) execution
- **Pattern(s):** time-half = `Delayed`/`DelayQueue` building block; dependency-half = topological sort (Kahn's algorithm), incremental/event-driven instead of batch
- **Unique data structure:** `DelayQueue` for time-based; in-degree counting via `AtomicInteger` + reverse-adjacency map for dependency-based
- **Concurrency move:** `AtomicInteger.decrementAndGet()` guarantees exactly-once dispatch on diamond dependencies; lock around cycle-check + graph registration
- **Gotcha:** periodic rescheduling happens AFTER execution finishes (fixed-delay), not immediately after submit (fixed-rate) — deliberate, avoids overlapping runs of a slow task.

## 8. Meeting Room Booking
- **Core ask:** search available rooms by criteria, book atomically, cancel
- **Pattern(s):** none formally named — straightforward atomic check-and-reserve orchestrator
- **Unique data structure:** per-room `TreeMap<startTime, Booking>` — non-overlapping invariant means a new request can only conflict with its floor/ceiling neighbor, O(log n)
- **Concurrency move:** per-room lock, check-and-reserve in ONE synchronized block
- **Gotcha:** `findAvailableRooms` is a stale snapshot for search UX only — the real safety guarantee lives entirely in `tryBook`'s atomic check-and-reserve, not the search step.

## 9. LRU Cache
- **Core ask:** O(1) get/put, evict least-recently-used at capacity
- **Pattern(s):** `Cache<K,V>` interface, swappable with LFU
- **Unique data structure:** `HashMap<key, Node>` + doubly linked list with sentinel head/tail
- **Concurrency move:** single global lock (not per-key) — operations mutate shared DLL ordering
- **Gotcha:** the HashMap maps key to the NODE itself, not the value — that's what makes both lookup AND reordering O(1). Sentinels eliminate null-checks at list boundaries.

## 10. LFU Cache
- **Core ask:** O(1) get/put, evict least-frequently-used, ties broken by least-recently-used
- **Pattern(s):** same `Cache<K,V>` interface as LRU
- **Unique data structure:** `HashMap<key, Node(freq)>` + `Map<frequency, LinkedHashSet<key>>` + `minFrequency` tracked incrementally
- **Concurrency move:** single global lock, same reasoning as LRU
- **Gotcha:** `LinkedHashSet`'s insertion order gives the LRU tie-break within a frequency bucket for free. `minFrequency` must update incrementally (never scan) — that's the O(1) eviction trick.