# Connection Pool — Mental Model (Backup Reference)

## 1. The One-Line Problem
Creating a DB connection is expensive (TCP handshake + auth). Doing it per-request exhausts the DB's connection limit and adds latency to every call. Fix: pre-create a bounded, reusable set of connections — a pool.

## 2. The Building Blocks

| Component | Type | Holds | Purpose |
|---|---|---|---|
| `ConnectionConfig` | data class | minSize, maxSize, borrowTimeoutMs, idleValidationIntervalMs | tuning knobs |
| `Connection` | interface | `isValid()`, `executeQuery()`, `close()` | contract every connection (real or proxy) honors |
| `RealDatabaseConnection` | implementation | connectionId, `closed` flag | the actual expensive resource |
| `ConnectionFactory` | interface | `createConnection()` | decouples "how a connection is made" from the pool |
| `DatabaseConnectionFactory` | implementation | atomic ID counter | produces `RealDatabaseConnection` instances |
| `PooledConnection` (proxy) | implementation | `delegate` (real connection), `ownerPool` (backpointer), `returned` flag | wraps a real connection so `close()` doesn't destroy it — releases it back to the pool instead |
| `ConnectionPool` | orchestrator | `freeConnections` (BlockingQueue), `activeConnections` (Set), `semaphore` (permits = maxSize), `lock` (ReentrantLock), factory, config | the brain — owns all state and all decisions |

## 3. Who Holds Who

```
ConnectionPool
 ├── holds → ConnectionFactory        (creates raw connections)
 ├── holds → BlockingQueue<Connection> freeConnections
 ├── holds → Set<Connection> activeConnections
 ├── holds → Semaphore(maxSize)
 └── holds → ReentrantLock

PooledConnection (the proxy)
 ├── wraps       → real Connection (delegate)
 └── points back → ConnectionPool (ownerPool)   <- this backpointer is what makes close() → release possible
```

## 4. Flow 1 — Pool Initialization
1. Pool constructed with config + factory.
2. Loop `minSize` times: acquire permit → factory creates real connection → offer to `freeConnections`.

Anchor: *warm up the minimum before anyone asks.*

## 5. Flow 2 — getConnection()
1. Check shutdown flag — reject if pool is closed.
2. `semaphore.tryAcquire(timeout)` — the only gate on total connection count. Timeout → throw `TimeoutException`. This is what turns "wait forever" into "bounded wait."
3. Poll `freeConnections` (non-blocking, permit already held):
   - `null` → grow the pool → factory creates a new one.
   - not null but `!isValid()` → stale, discard, factory creates a replacement.
   - not null and valid → reuse as-is.
4. Wrap the raw connection in a new `PooledConnection(raw, this)`.
5. Add the `PooledConnection` to `activeConnections`.
6. Return the `PooledConnection` to the caller.

Anchor: *permit first, queue second, wrap last.*

## 6. Flow 3 — close() / release (the proxy trick)
Caller never knows this is happening — they just call `.close()` like normal JDBC.

1. `PooledConnection.close()` checks the `returned` flag:
   - already `true` → no-op (guards against double release).
   - `false` → set `returned = true`, call `ownerPool.releaseConnection(this)`.
2. Inside `ConnectionPool.releaseConnection()` (under lock):
   - Remove from `activeConnections`.
   - Fetch the delegate (real connection).
   - If `delegate.isValid()` AND pool not shutdown → offer back to `freeConnections` (reuse). Else → physically close the delegate (discard).
3. `finally`: unlock, then `permits.release()` — order matters; state must be consistent before a waiting thread wakes up.

Anchor: *caller thinks destroy, pool actually recycles.*

## 7. Flow 4 — shutdown()
1. Acquire lock.
2. Set `shutdown = true`.
3. Close and clear every connection in `freeConnections`.
4. Close and clear every connection in `activeConnections`.
5. Unlock.

Anchor: *no orphan sockets left behind.*

## 8. Pattern Cheat Sheet (say these exact words in the interview)
- **Object Pool** — the overall pattern name for the whole problem.
- **Factory** — decouples connection creation from the pool's logic.
- **Proxy/Decorator** — `PooledConnection` wraps `Connection`, intercepts `close()` only.
- **NOT a proxy:** the factory. Don't call it that.

## 9. Concurrency Cheat Sheet
- `Semaphore(maxSize)` → bounds total live connections, gives timed/bounded wait.
- `BlockingQueue` → safe concurrent offer/poll of free connections.
- `ConcurrentHashMap.newKeySet()` → safe concurrent add/remove of active connections.
- `ReentrantLock` → protects the compound release/shutdown state transition (semaphore only guards *count*, not queue/set *mutation*).
- `returned` flag on `PooledConnection` → guards against double-release corrupting state.

## 10. Bugs to Never Repeat (from your last two coding attempts)
- Missing return types on interface/impl methods (compile error).
- Field/method name collision (`close` field vs `close()` method) — name the field `closed`.
- Inverted `isValid()` check inside `executeQuery` — valid should run the query, invalid should reject.
- `owner` field typed as `Connection` instead of a pool reference — breaks the ability to release.
- `isValid()` on `PooledConnection` checking `returned` instead of delegating to `delegate.isValid()`.
- Calling the Factory a "Proxy" — it isn't one; only `PooledConnection` is.

## 11. 10-Second Recall Sentence
"Pool owns a queue of free connections and a set of active ones, guarded by a semaphore for count and a lock for safe mutation; `getConnection` wraps a raw connection in a proxy so `close()` releases instead of destroys."
