# 🔐 COMPLETE JAVA CONCURRENCY & LOCKS MASTERY GUIDE
## Threads • Locks • Semaphores • Concurrent Collections • LLD Applications
**Your Complete Reference — From Zero to Production Level**

---

# TABLE OF CONTENTS

1. [THREADS — The Foundation](#threads)
2. [THE PROBLEM — Why Locks Exist](#why-locks)
3. [SYNCHRONIZED — The Simplest Lock](#synchronized)
4. [REENTRANT LOCK — Full Control](#reentrant-lock)
5. [READ-WRITE LOCK — Maximum Throughput](#read-write-lock)
6. [SEMAPHORE — Counting Gate](#semaphore)
7. [STAMPED LOCK — Optimistic Reading](#stamped-lock)
8. [CONDITION — Wait and Signal](#condition)
9. [ATOMIC VARIABLES — Lock-Free](#atomic)
10. [CONCURRENT COLLECTIONS](#concurrent-collections)
11. [EXECUTOR SERVICE — Thread Pools](#executor)
12. [VOLATILE — Visibility](#volatile)
13. [DEADLOCKS — Detection and Prevention](#deadlocks)
14. [VIRTUAL THREADS (Java 19+)](#virtual-threads)
15. [COMPLETE COMPARISON TABLE](#comparison)
16. [LLD APPLICATION — Where to Use What](#lld-application)

---

<a name="threads"></a>
# 1. THREADS — THE FOUNDATION

## What is a Thread?

A thread is the smallest unit of execution. Your JVM can run
multiple threads simultaneously — each doing its own work.

```
Single-threaded program:
  Task A → Task B → Task C → Done
  (sequential, one at a time)

Multi-threaded program:
  Thread 1: Task A ──────────────→ Done
  Thread 2:    Task B ─────────→ Done
  Thread 3:       Task C ────→ Done
  (parallel, overlapping)
```

## Creating Threads — 4 Ways

### Way 1: Extend Thread
```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Running in: " + Thread.currentThread().getName());
    }
}

MyThread t = new MyThread();
t.start();  // starts new thread — calls run() in new thread
// t.run() would run in CURRENT thread — wrong!
```

### Way 2: Implement Runnable (Preferred)
```java
Runnable task = () -> {
    System.out.println("Running in: " + Thread.currentThread().getName());
};

Thread t = new Thread(task);
t.start();
```

### Way 3: Callable (Returns a Value)
```java
Callable<Integer> task = () -> {
    Thread.sleep(1000);
    return 42;
};

ExecutorService executor = Executors.newFixedThreadPool(2);
Future<Integer> future = executor.submit(task);

Integer result = future.get();  // Blocks until result is ready
System.out.println(result);     // 42
```

### Way 4: Virtual Threads (Java 19+)
```java
Thread vThread = Thread.ofVirtual().start(() -> {
    System.out.println("Virtual thread — lightweight");
});
```

---

## Thread Lifecycle — All States

```
NEW ──── start() ────▶ RUNNABLE ──── scheduler ────▶ RUNNING
                           ▲                              │
                           │          sleep/wait/block    │
                           └──────── BLOCKED/WAITING ◀───┘
                                          │
                                    notify/interrupt
                                          │
                                       RUNNABLE
                                          │
                                      run() ends
                                          │
                                      TERMINATED
```

### Thread States Explained:
```java
Thread t = new Thread(() -> {});

// NEW — created but not started
System.out.println(t.getState());  // NEW

t.start();
// RUNNABLE — eligible to run, waiting for CPU

// BLOCKED — waiting for a monitor lock (synchronized)
// WAITING — waiting indefinitely (wait(), join())
// TIMED_WAITING — waiting for specified time (sleep(), wait(timeout))
// TERMINATED — finished execution
```

---

## Important Thread Methods

```java
Thread t = new Thread(task);

// Lifecycle
t.start();                  // Start thread (non-blocking)
t.join();                   // Wait for thread to finish (blocks caller)
t.join(5000);               // Wait max 5 seconds
t.interrupt();              // Send interrupt signal to thread
t.isAlive();                // Is thread still running?
t.getState();               // Get current state

// Thread info
t.getName();                // "Thread-0" or custom name
t.setName("worker-1");      // Set custom name
t.getPriority();            // 1 (MIN) to 10 (MAX), default 5
t.setPriority(8);           // Set priority
t.isDaemon();               // Is it a daemon thread?
t.setDaemon(true);          // Mark as daemon (dies when main dies)

// Static methods
Thread.sleep(1000);         // Pause current thread for 1 second
Thread.currentThread();     // Get reference to current thread
Thread.yield();             // Hint to scheduler to let others run
Thread.activeCount();       // Count active threads
```

---

## Thread — Real Example with Your Work Context

```java
// How TechMojo might process transactions concurrently
public class TransactionProcessor {

    private final List<Transaction> pendingTransactions;

    public void processAll() throws InterruptedException {
        // Split into batches and process in parallel
        List<Thread> workers = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            final int workerId = i;
            Thread worker = new Thread(() -> {
                processBatch(workerId);
            }, "tx-worker-" + i);

            workers.add(worker);
            worker.start();
        }

        // Wait for all workers to finish
        for (Thread worker : workers) {
            worker.join();  // Main thread waits here
        }

        System.out.println("All transactions processed");
    }

    private void processBatch(int workerId) {
        System.out.println("Worker " + workerId + " processing batch");
        // Process transactions...
    }
}
```

---

<a name="why-locks"></a>
# 2. THE PROBLEM — WHY LOCKS EXIST

## The Race Condition — The Core Problem

```java
// BROKEN — no synchronization
public class BankAccount {
    private int balance = 1000;

    public void withdraw(int amount) {
        if (balance >= amount) {        // Step 1: Check
            balance = balance - amount; // Step 2: Deduct
        }
    }
}

// Two threads withdraw 700 each simultaneously:

// Thread 1: balance=1000, checks 1000 >= 700 → TRUE
// Thread 2: balance=1000, checks 1000 >= 700 → TRUE (same time!)
// Thread 1: balance = 1000 - 700 = 300
// Thread 2: balance =  300 - 700 = -400  ← OVERDRAFT! BUG!
```

This is a **race condition** — the final result depends on which
thread runs first. The outcome is non-deterministic and wrong.

## Three Problems in Concurrency:

```
1. RACE CONDITION
   Multiple threads read-modify-write same data
   Result depends on scheduling order
   Fix: Synchronization (locks)

2. VISIBILITY PROBLEM
   Thread A writes value
   Thread B reads OLD cached value (CPU cache)
   Fix: volatile keyword

3. DEADLOCK
   Thread A holds Lock 1, waits for Lock 2
   Thread B holds Lock 2, waits for Lock 1
   Both wait forever — program hangs
   Fix: Lock ordering, timeouts
```

---

<a name="synchronized"></a>
# 3. SYNCHRONIZED — THE SIMPLEST LOCK

## What It Does

`synchronized` ensures only ONE thread executes a block at a time.
Every Java object has a built-in monitor lock.

## Method-Level Synchronization

```java
public class BankAccount {
    private int balance = 1000;

    // Entire method is locked — only one thread at a time
    public synchronized void withdraw(int amount) {
        if (balance >= amount) {
            balance = balance - amount;
        }
    }

    public synchronized void deposit(int amount) {
        balance = balance + amount;
    }

    public synchronized int getBalance() {
        return balance;
    }
}

// Both withdraw() and deposit() use the SAME lock (this object)
// So they can never run simultaneously — even different methods
```

## Block-Level Synchronization

```java
public class BankAccount {
    private int balance = 1000;
    private final Object balanceLock = new Object();  // dedicated lock

    public void withdraw(int amount) {
        // Only the critical section is locked
        // Other non-critical code can run concurrently
        System.out.println("Starting withdrawal");  // Not locked

        synchronized (balanceLock) {
            if (balance >= amount) {
                balance = balance - amount;
            }
        }  // Lock released here

        System.out.println("Withdrawal complete");  // Not locked
    }
}
```

## Static Synchronization — Class Level Lock

```java
public class Counter {
    private static int count = 0;

    // Locks on Counter.class (not instance)
    public static synchronized void increment() {
        count++;
    }

    // Equivalent:
    public static void decrement() {
        synchronized (Counter.class) {
            count--;
        }
    }
}
```

## Synchronized — Important Facts

```
✅ Simple to use
✅ Automatic lock/unlock (even on exception)
✅ Works on any object
✅ Reentrant — same thread can enter multiple times

❌ No timeout — waits forever
❌ No way to check if lock is available
❌ Cannot interrupt a waiting thread
❌ No read/write distinction — reads block each other
❌ No fairness — any waiting thread can get lock next
```

---

<a name="reentrant-lock"></a>
# 4. REENTRANT LOCK — FULL CONTROL

## What is ReentrantLock?

ReentrantLock gives you everything synchronized does,
plus timeout, interruptibility, fairness, and manual control.

"Reentrant" means the same thread can acquire the lock
multiple times without deadlocking itself.

```java
import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    private int balance = 1000;
    private final ReentrantLock lock = new ReentrantLock();

    public void withdraw(int amount) {
        lock.lock();        // Acquire lock
        try {
            if (balance >= amount) {
                balance = balance - amount;
            }
        } finally {
            lock.unlock();  // ALWAYS in finally — even if exception thrown
        }
    }
}
```

## All ReentrantLock Methods

```java
ReentrantLock lock = new ReentrantLock();
ReentrantLock fairLock = new ReentrantLock(true);  // Fair mode

// Acquire
lock.lock();                // Block until lock acquired (no timeout)
lock.lockInterruptibly();   // Block, but can be interrupted
boolean got = lock.tryLock();              // Try now, return false if busy
boolean got = lock.tryLock(5, TimeUnit.SECONDS);  // Try for 5 seconds

// Release
lock.unlock();              // Release lock — ALWAYS in finally

// Status
lock.isLocked();            // Is lock currently held by anyone?
lock.isHeldByCurrentThread(); // Does THIS thread hold it?
lock.getHoldCount();        // How many times current thread locked it
lock.getQueueLength();      // How many threads waiting for this lock
lock.hasQueuedThreads();    // Any threads waiting?
lock.hasQueuedThread(t);    // Is specific thread waiting?
```

## Reentrant Behavior Explained

```java
public class ReentrantExample {
    private final ReentrantLock lock = new ReentrantLock();

    public void methodA() {
        lock.lock();
        try {
            System.out.println("In A, holdCount=" + lock.getHoldCount()); // 1
            methodB();  // Same thread calls B — lock acquired again
            System.out.println("Back in A, holdCount=" + lock.getHoldCount()); // 1
        } finally {
            lock.unlock();
        }
    }

    public void methodB() {
        lock.lock();  // Same thread — NOT blocked, just increments count
        try {
            System.out.println("In B, holdCount=" + lock.getHoldCount()); // 2
        } finally {
            lock.unlock();  // Decrements count to 1 (doesn't fully release)
        }
    }
}

// Thread calls methodA():
// A: lock acquired, holdCount=1
// A calls B: lock acquired again, holdCount=2
// B: unlock, holdCount=1 (still locked!)
// A: unlock, holdCount=0 (fully released)
// Another thread can now acquire
```

## tryLock — The Key Production Pattern

```java
public class ConnectionPool {
    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<Connection> pool = new LinkedList<>();

    public Connection getConnection(long timeoutMs) throws Exception {
        // Try to acquire lock with timeout
        boolean locked = lock.tryLock(timeoutMs, TimeUnit.MILLISECONDS);

        if (!locked) {
            throw new TimeoutException("Could not acquire pool lock in " + timeoutMs + "ms");
        }

        try {
            Connection conn = pool.poll();
            if (conn == null) {
                throw new NoConnectionAvailableException("Pool is empty");
            }
            return conn;
        } finally {
            lock.unlock();
        }
    }
}
```

## Fair vs Unfair Lock

```java
// Unfair (default) — any waiting thread can jump queue
// Better throughput but possible starvation
ReentrantLock unfairLock = new ReentrantLock(false);

// Fair — threads served in order they requested (FIFO)
// Lower throughput but prevents starvation
ReentrantLock fairLock = new ReentrantLock(true);

// Production: Use unfair (default) unless starvation is a real problem
// Fair locks can be 5-10x slower due to scheduling overhead
```

## synchronized vs ReentrantLock

```
Feature                    synchronized    ReentrantLock
─────────────────────────────────────────────────────────
Timeout on acquire         ❌ No           ✅ tryLock(timeout)
Can interrupt waiting      ❌ No           ✅ lockInterruptibly()
Fairness option            ❌ No           ✅ new ReentrantLock(true)
Try without blocking       ❌ No           ✅ tryLock()
Multiple conditions        ❌ One (wait)   ✅ Multiple Condition objects
Check lock status          ❌ No           ✅ isLocked(), getHoldCount()
Automatic release          ✅ Yes          ❌ Must call unlock() in finally
Syntax                     ✅ Simple       ❌ More verbose
Performance (low contention) ✅ Faster    ❌ Slightly slower
```

### Rule of Thumb:
```
Use synchronized when:
  - Simple locking, low complexity
  - Don't need timeout or interrupt

Use ReentrantLock when:
  - Need timeout (tryLock)
  - Need to interrupt waiting threads
  - Need fairness
  - Need multiple condition variables
  - Need to check lock status
```

---

<a name="read-write-lock"></a>
# 5. READ-WRITE LOCK — MAXIMUM THROUGHPUT

## The Problem with Regular Locks

```java
// With synchronized — reads block each other
// Thread 1 reading → Thread 2 WAITS to read
// This is unnecessary! Two reads never conflict!

// Bank balance read:
public synchronized int getBalance() {
    return balance;  // Thread 2 waits even though this is just a read!
}
```

## ReadWriteLock — The Solution

```java
// RULE:
// Multiple threads can READ simultaneously    (Read lock — shared)
// Only ONE thread can WRITE at a time         (Write lock — exclusive)
// While writing, no reading allowed           (Write blocks reads)
// While reading, no writing allowed           (Read blocks writes)

// Perfect for: Caches, configuration, shared data with many reads
```

```java
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserCache {
    private final Map<Long, User> cache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock  = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    // READ — multiple threads can do this simultaneously
    public User getUser(Long userId) {
        readLock.lock();
        try {
            return cache.get(userId);  // Multiple threads read at once ✅
        } finally {
            readLock.unlock();
        }
    }

    // WRITE — only one thread at a time, blocks all reads
    public void putUser(Long userId, User user) {
        writeLock.lock();
        try {
            cache.put(userId, user);  // Exclusive access ✅
        } finally {
            writeLock.unlock();
        }
    }

    // WRITE — remove
    public void removeUser(Long userId) {
        writeLock.lock();
        try {
            cache.remove(userId);
        } finally {
            writeLock.unlock();
        }
    }
}
```

## Read-Write Lock Visualization

```
Timeline:

T1 reads  ══════════╗
T2 reads  ══════════╣ All reading simultaneously ✅
T3 reads  ══════════╝
T4 WRITES           ║ Waits for reads to finish
                    ╚══════════╗ Exclusive write
T5 reads                      ╚════════ Waits for write
T6 reads                      ╚════════ Waits for write
                                        Both read simultaneously ✅
```

## All ReadWriteLock Methods

```java
ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
ReentrantReadWriteLock rwFairLock = new ReentrantReadWriteLock(true); // Fair

Lock readLock  = rwLock.readLock();
Lock writeLock = rwLock.writeLock();

// Read lock
readLock.lock();
readLock.unlock();
readLock.tryLock();
readLock.tryLock(5, TimeUnit.SECONDS);
readLock.lockInterruptibly();

// Write lock
writeLock.lock();
writeLock.unlock();
writeLock.tryLock();
writeLock.tryLock(5, TimeUnit.SECONDS);
writeLock.lockInterruptibly();

// Status
rwLock.getReadLockCount();     // How many threads hold read lock
rwLock.isWriteLocked();        // Is write lock held?
rwLock.getWriteHoldCount();    // How many times write lock acquired
rwLock.getReadHoldCount();     // Read count for current thread
rwLock.getQueueLength();       // Threads waiting for write lock
```

## Production Example — Your Redis-Like Cache

```java
public class DistributedCache<K, V> {

    private final Map<K, V> store = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock  = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    // Reads — concurrent (your Redis cache layer at TechMojo)
    public V get(K key) {
        readLock.lock();
        try {
            return store.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public boolean containsKey(K key) {
        readLock.lock();
        try {
            return store.containsKey(key);
        } finally {
            readLock.unlock();
        }
    }

    // Writes — exclusive
    public void put(K key, V value) {
        writeLock.lock();
        try {
            store.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }

    public V remove(K key) {
        writeLock.lock();
        try {
            return store.remove(key);
        } finally {
            writeLock.unlock();
        }
    }

    // Read-then-write pattern — must use write lock
    public V putIfAbsent(K key, V value) {
        writeLock.lock();  // Write lock for both check and put
        try {
            if (!store.containsKey(key)) {
                store.put(key, value);
                return null;
            }
            return store.get(key);
        } finally {
            writeLock.unlock();
        }
    }

    // Get snapshot — read lock
    public Map<K, V> getAll() {
        readLock.lock();
        try {
            return new HashMap<>(store);  // Return copy
        } finally {
            readLock.unlock();
        }
    }
}

// Thread safety profile:
// 1000 reads/second, 10 writes/second
// With synchronized: reads block each other → slow
// With ReadWriteLock: 1000 reads run simultaneously → fast
```

---

<a name="semaphore"></a>
# 6. SEMAPHORE — THE COUNTING GATE

## What Semaphore Does

Controls how many threads access a resource simultaneously.
Like a bouncer with N wristbands — only N people inside at once.

```java
import java.util.concurrent.Semaphore;

// Allow max 3 threads simultaneously
Semaphore semaphore = new Semaphore(3);

semaphore.acquire();    // Get one permit (block if 0 permits left)
semaphore.release();    // Return one permit (wake a waiting thread)
```

## All Semaphore Methods

```java
Semaphore sem = new Semaphore(10);         // 10 permits
Semaphore fairSem = new Semaphore(10, true); // Fair mode (FIFO)

// Acquire (consume permit)
sem.acquire();                    // Block until permit available
sem.acquire(3);                   // Acquire 3 permits at once
sem.acquireUninterruptibly();     // Block, ignore interrupts
boolean got = sem.tryAcquire();   // Try now, false if unavailable
boolean got = sem.tryAcquire(5, TimeUnit.SECONDS);  // With timeout
boolean got = sem.tryAcquire(3, 5, TimeUnit.SECONDS); // 3 permits, timeout

// Release (return permit)
sem.release();                    // Return 1 permit
sem.release(3);                   // Return 3 permits

// Status
sem.availablePermits();           // How many permits available now
sem.hasQueuedThreads();           // Any threads waiting?
sem.getQueueLength();             // How many threads waiting?
sem.isFair();                     // Is it fair mode?

// Drain (take all permits)
int acquired = sem.drainPermits(); // Take all available permits
```

## Full Connection Pool with Semaphore

```java
public class ConnectionPool {

    private final int maxSize;
    private final Semaphore semaphore;          // Controls access count
    private final BlockingQueue<Connection> freeConnections;
    private final Set<Connection> activeConnections;
    private final ReentrantLock poolLock;       // Protects activeConnections set

    public ConnectionPool(String url, int maxSize) throws SQLException {
        this.maxSize           = maxSize;
        this.semaphore         = new Semaphore(maxSize, true); // Fair
        this.freeConnections   = new LinkedBlockingQueue<>(maxSize);
        this.activeConnections = new HashSet<>();
        this.poolLock          = new ReentrantLock();

        // Pre-create all connections
        for (int i = 0; i < maxSize; i++) {
            freeConnections.offer(DriverManager.getConnection(url));
        }
    }

    public Connection borrow(long timeoutMs) throws Exception {
        // Step 1: Acquire permit — block if pool full
        boolean acquired = semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        if (!acquired) {
            throw new TimeoutException("Pool exhausted. No connection in " + timeoutMs + "ms");
        }

        // Step 2: Get connection from free queue
        Connection conn = freeConnections.poll();
        if (conn == null || !isValid(conn)) {
            conn = createNewConnection();  // Reconnect if stale
        }

        // Step 3: Track as active
        poolLock.lock();
        try {
            activeConnections.add(conn);
        } finally {
            poolLock.unlock();
        }

        return conn;
    }

    public void returnConnection(Connection conn) {
        if (conn == null) return;

        // Step 1: Remove from active
        poolLock.lock();
        try {
            activeConnections.remove(conn);
        } finally {
            poolLock.unlock();
        }

        // Step 2: Return to free queue
        freeConnections.offer(conn);

        // Step 3: Release permit — wakes ONE waiting thread
        semaphore.release();
    }

    private boolean isValid(Connection conn) {
        try {
            return conn.isValid(2);  // 2 second timeout
        } catch (SQLException e) {
            return false;
        }
    }

    public int getAvailable() { return semaphore.availablePermits(); }
    public int getActive() {
        poolLock.lock();
        try { return activeConnections.size(); }
        finally { poolLock.unlock(); }
    }
}
```

## Binary Semaphore vs Mutex

```java
// Binary Semaphore (1 permit) — works like a mutex
Semaphore mutex = new Semaphore(1);

mutex.acquire();
try {
    // Critical section — only 1 thread
} finally {
    mutex.release();
}

// Key difference from ReentrantLock:
// Semaphore can be released by DIFFERENT thread
// Lock must be released by thread that acquired it

// Use case: Producer releases, Consumer acquires
Semaphore signal = new Semaphore(0);  // Start with 0

// Producer thread
data = produceData();
signal.release();  // Signal consumer

// Consumer thread
signal.acquire();  // Wait for producer
consume(data);
```

---

<a name="stamped-lock"></a>
# 7. STAMPED LOCK — OPTIMISTIC READING (Java 8+)

## The Problem with ReadWriteLock

```
ReadWriteLock issue:
  Reads still take a lock (though shared)
  For 99% read workload, even read locks have overhead

StampedLock solution:
  Optimistic read — no lock at all!
  Read data, validate it wasn't changed, retry if it was
  Only convert to real lock if validation fails
```

```java
import java.util.concurrent.locks.StampedLock;

public class Point {
    private double x, y;
    private final StampedLock lock = new StampedLock();

    // WRITE — exclusive
    public void move(double deltaX, double deltaY) {
        long stamp = lock.writeLock();  // Returns a stamp (token)
        try {
            x += deltaX;
            y += deltaY;
        } finally {
            lock.unlockWrite(stamp);    // Must pass stamp back
        }
    }

    // OPTIMISTIC READ — no actual lock!
    public double distanceFromOrigin() {
        long stamp = lock.tryOptimisticRead(); // Get a stamp (no lock)
        double currentX = x;
        double currentY = y;

        // Validate: was the stamp invalidated by a write?
        if (!lock.validate(stamp)) {
            // A write happened while we were reading — retry with real lock
            stamp = lock.readLock();
            try {
                currentX = x;
                currentY = y;
            } finally {
                lock.unlockRead(stamp);
            }
        }

        return Math.sqrt(currentX * currentX + currentY * currentY);
    }

    // READ — shared (like ReadWriteLock)
    public double getX() {
        long stamp = lock.readLock();
        try {
            return x;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    // CONVERT read to write (upgrade lock)
    public void moveIfOrigin(double newX, double newY) {
        long stamp = lock.readLock();
        try {
            while (x == 0.0 && y == 0.0) {
                // Try to upgrade to write lock
                long writeStamp = lock.tryConvertToWriteLock(stamp);
                if (writeStamp != 0L) {
                    stamp = writeStamp;
                    x = newX;
                    y = newY;
                    break;
                } else {
                    // Upgrade failed — release read, get write
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                }
            }
        } finally {
            lock.unlock(stamp);
        }
    }
}
```

## StampedLock vs ReadWriteLock

```
Feature              ReadWriteLock    StampedLock
───────────────────────────────────────────────────
Optimistic read      ❌ No            ✅ Yes (no lock)
Lock upgrade         ❌ No            ✅ tryConvertToWriteLock
Reentrant            ✅ Yes           ❌ NOT reentrant
Condition support    ✅ Yes           ❌ No
Complexity           Simple           Complex
Performance          Good             Best (read-heavy)
```

### When to Use:
```
Use StampedLock when:
  - 95%+ reads, very few writes
  - Performance is critical
  - You can handle the extra complexity

Use ReadWriteLock when:
  - Mix of reads and writes
  - Need reentrancy
  - Need Condition objects
  - Simpler code preferred
```

---

<a name="condition"></a>
# 8. CONDITION — WAIT AND SIGNAL

## What is a Condition?

A Condition lets threads wait for a specific state to be true,
then be signalled when it changes.

Think of it as: "I'll wait here until someone tells me conditions are right."

## With synchronized — wait/notify

```java
public class BlockingQueue<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    // Producer — wait if full
    public synchronized void put(T item) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();  // Release lock and wait
        }
        queue.offer(item);
        notifyAll();  // Wake all waiting consumers
    }

    // Consumer — wait if empty
    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();  // Release lock and wait
        }
        T item = queue.poll();
        notifyAll();  // Wake all waiting producers
        return item;
    }
}
```

## With ReentrantLock — Condition objects

```java
// Better: separate conditions for full and empty
public class BoundedBuffer<T> {
    private final Queue<T> queue = new LinkedList<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();

    // TWO separate conditions — more efficient than notifyAll
    private final Condition notFull  = lock.newCondition(); // signal when not full
    private final Condition notEmpty = lock.newCondition(); // signal when not empty

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
    }

    // Producer
    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            // Wait while FULL
            while (queue.size() == capacity) {
                notFull.await();  // Release lock, wait for notFull signal
            }
            queue.offer(item);
            notEmpty.signal();  // Signal ONE waiting consumer
        } finally {
            lock.unlock();
        }
    }

    // Consumer
    public T take() throws InterruptedException {
        lock.lock();
        try {
            // Wait while EMPTY
            while (queue.isEmpty()) {
                notEmpty.await();  // Release lock, wait for notEmpty signal
            }
            T item = queue.poll();
            notFull.signal();   // Signal ONE waiting producer
            return item;
        } finally {
            lock.unlock();
        }
    }
}
```

## Condition Methods

```java
ReentrantLock lock = new ReentrantLock();
Condition condition = lock.newCondition();

// Must hold lock before calling these:

// Wait
condition.await();                          // Wait indefinitely
condition.await(5, TimeUnit.SECONDS);       // Wait with timeout
condition.awaitUninterruptibly();           // Wait, ignore interrupts
condition.awaitNanos(5_000_000_000L);       // Wait in nanoseconds
boolean stillWaiting = condition.awaitUntil(deadline); // Wait until date

// Signal
condition.signal();     // Wake ONE waiting thread
condition.signalAll();  // Wake ALL waiting threads
```

## Why Condition > wait/notify

```
wait/notify:
  - One condition per object
  - notifyAll wakes ALL threads (inefficient)
  - No timeout on notify

Condition:
  - Multiple conditions per lock
  - signal() wakes only threads waiting on THAT condition
  - await(timeout) with duration
  - Much more precise control
```

---

<a name="atomic"></a>
# 9. ATOMIC VARIABLES — LOCK-FREE OPERATIONS

## What are Atomic Variables?

Atomic variables perform read-modify-write operations
as a single, indivisible CPU instruction — NO LOCK NEEDED.

Uses hardware CAS (Compare-And-Swap) instruction.

```java
import java.util.concurrent.atomic.*;

// Instead of:
private int count = 0;
public synchronized void increment() { count++; }

// Use:
private AtomicInteger count = new AtomicInteger(0);
public void increment() { count.incrementAndGet(); }  // Lock-free!
```

## All Atomic Types

```java
// Integer
AtomicInteger ai = new AtomicInteger(0);
ai.get();                     // Read
ai.set(10);                   // Write
ai.getAndSet(5);              // Get old value, set new
ai.incrementAndGet();         // ++i (returns new value)
ai.getAndIncrement();         // i++ (returns old value)
ai.decrementAndGet();         // --i
ai.getAndDecrement();         // i--
ai.addAndGet(5);              // i+=5 (returns new)
ai.getAndAdd(5);              // i+=5 (returns old)
ai.compareAndSet(5, 10);      // If value==5, set to 10 (returns success)

// Long
AtomicLong al = new AtomicLong(0L);
// Same methods as AtomicInteger

// Boolean
AtomicBoolean ab = new AtomicBoolean(false);
ab.get();
ab.set(true);
ab.getAndSet(true);           // Returns old value
ab.compareAndSet(false, true); // CAS

// Reference (any object)
AtomicReference<User> ref = new AtomicReference<>(user);
ref.get();
ref.set(newUser);
ref.compareAndSet(expectedUser, newUser);  // CAS on reference

// Integer Array
AtomicIntegerArray arr = new AtomicIntegerArray(10);
arr.get(0);
arr.set(0, 5);
arr.getAndIncrement(0);
arr.compareAndSet(0, 5, 10);
```

## compareAndSet — The Core Operation

```java
// CAS: if current value == expected, set to new value
// Returns true if successful

AtomicInteger counter = new AtomicInteger(5);

boolean success = counter.compareAndSet(5, 10);
// current=5, expected=5, match → set to 10 → true

boolean success2 = counter.compareAndSet(5, 20);
// current=10, expected=5, no match → not changed → false

// CAS loop pattern — optimistic concurrency
public void safeIncrement(AtomicInteger counter) {
    while (true) {
        int current = counter.get();
        int next = current + 1;
        if (counter.compareAndSet(current, next)) {
            break;  // Success!
        }
        // Another thread changed it — retry
    }
    // OR simply:
    counter.incrementAndGet();  // Does CAS internally
}
```

## Production Use — Ticket Counter (Your TechMojo Context)

```java
public class TicketCounter {
    private final AtomicInteger ticketNumber = new AtomicInteger(1000);
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final AtomicBoolean isOpen = new AtomicBoolean(true);

    // Thread-safe, lock-free ticket generation
    public int nextTicket() {
        if (!isOpen.get()) {
            throw new IllegalStateException("Counter is closed");
        }
        return ticketNumber.getAndIncrement();
    }

    public void processTicket() {
        totalProcessed.incrementAndGet();
    }

    public void close() {
        isOpen.set(false);
    }

    public long getTotalProcessed() {
        return totalProcessed.get();
    }
}
```

---

<a name="concurrent-collections"></a>
# 10. CONCURRENT COLLECTIONS

## ConcurrentHashMap

Thread-safe HashMap — does NOT lock the entire map.
Uses segment-level locking (Java 7) / CAS + bin locking (Java 8+).

```java
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

// All standard Map operations are thread-safe
map.put("key", 1);
map.get("key");
map.remove("key");
map.containsKey("key");

// Atomic operations — critical for concurrent use
map.putIfAbsent("key", 1);           // Put only if not present (atomic)
map.replace("key", 1, 2);           // Replace only if value matches (atomic)
map.computeIfAbsent("key", k -> 0); // Compute and put if absent (atomic)
map.merge("key", 1, Integer::sum);  // Merge with existing value (atomic)

// Iteration — weakly consistent (safe but may not reflect latest changes)
map.forEach((k, v) -> System.out.println(k + "=" + v));
map.entrySet().forEach(e -> {});

// Size (approximate — not guaranteed exact in concurrent use)
map.size();
map.mappingCount();  // Better for large maps (returns long)

// Parallel operations (Java 8+)
map.forEach(2, (k, v) -> process(k, v));  // Parallel with threshold 2
map.reduce(1, (k, v) -> v, Integer::sum); // Parallel reduce
```

## CopyOnWriteArrayList

For lists that are rarely written but frequently read.
Every write creates a new copy of the array.

```java
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();

list.add("item");       // Creates new array copy
list.remove("item");    // Creates new array copy
list.get(0);            // Reads current snapshot — no lock
list.size();            // Reads current snapshot — no lock

// Iteration is safe — iterates over snapshot
for (String item : list) {
    // Modifying list here is safe — iterates old snapshot
    list.remove(item);  // Won't throw ConcurrentModificationException
}

// When to use:
// - Listener lists (many reads, rare additions)
// - Configuration lists
// NOT for: high-frequency writes (expensive copy every time)
```

## BlockingQueue — For Producer-Consumer

```java
// LinkedBlockingQueue — unbounded or bounded
BlockingQueue<Task> queue = new LinkedBlockingQueue<>(100);

// Producer
queue.put(task);         // Block if full
queue.offer(task);       // Return false if full (no wait)
queue.offer(task, 5, TimeUnit.SECONDS);  // Wait up to 5s

// Consumer
Task task = queue.take();       // Block if empty
Task task = queue.poll();       // Return null if empty
Task task = queue.poll(5, TimeUnit.SECONDS); // Wait up to 5s
Task task = queue.peek();       // View without removing

// Other BlockingQueue implementations
ArrayBlockingQueue<Task> arrayQueue = new ArrayBlockingQueue<>(100);
PriorityBlockingQueue<Task> priorityQueue = new PriorityBlockingQueue<>();
DelayQueue<DelayedTask> delayQueue = new DelayQueue<>();
SynchronousQueue<Task> syncQueue = new SynchronousQueue<>(); // No buffer
```

## ConcurrentLinkedQueue — Lock-Free Queue

```java
ConcurrentLinkedQueue<Task> queue = new ConcurrentLinkedQueue<>();

queue.offer(task);      // Add to tail — never blocks
queue.poll();           // Remove from head — returns null if empty
queue.peek();           // View head — returns null if empty
queue.size();           // O(n) — expensive!
queue.isEmpty();        // O(1) — cheap
```

---

<a name="executor"></a>
# 11. EXECUTOR SERVICE — THREAD POOLS

## Why Thread Pools?

```
Creating a thread is EXPENSIVE:
  - OS allocates stack (512KB default)
  - JVM setup
  - ~1ms overhead per thread

Thread pool reuses threads:
  - Create N threads once
  - Reuse for thousands of tasks
  - Much faster per-task
```

```java
// Fixed thread pool — N threads always alive
ExecutorService pool = Executors.newFixedThreadPool(10);

// Cached thread pool — creates threads as needed, reuses idle
ExecutorService pool = Executors.newCachedThreadPool();

// Single thread — one thread, tasks queued
ExecutorService pool = Executors.newSingleThreadExecutor();

// Scheduled — run tasks after delay or periodically
ScheduledExecutorService pool = Executors.newScheduledThreadPool(5);

// Virtual thread pool (Java 21) — millions of tasks
ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor();
```

## Submitting Tasks

```java
ExecutorService executor = Executors.newFixedThreadPool(10);

// Submit Runnable — no return value
executor.execute(() -> processTransaction());

// Submit Callable — returns Future
Future<Result> future = executor.submit(() -> {
    return processAndReturn();
});

// Get result (blocks until done)
Result result = future.get();
Result result = future.get(5, TimeUnit.SECONDS); // With timeout

// Check status
future.isDone();       // Completed?
future.isCancelled();  // Was cancelled?
future.cancel(true);   // Cancel (interrupt if running)

// Submit multiple — get all results
List<Callable<Result>> tasks = Arrays.asList(task1, task2, task3);
List<Future<Result>> futures = executor.invokeAll(tasks);

// First completed wins
Result first = executor.invokeAny(tasks);

// Shutdown
executor.shutdown();          // No new tasks, finish existing
executor.shutdownNow();       // Interrupt all running tasks
executor.awaitTermination(10, TimeUnit.SECONDS);
```

## CompletableFuture — Async Chaining (Java 8+)

```java
// Chain async operations — like your Kafka event workflows
CompletableFuture<User> future = CompletableFuture
    .supplyAsync(() -> userRepository.findById(1L))     // Async fetch
    .thenApply(user -> enrichUser(user))                 // Transform
    .thenApply(user -> validateUser(user))               // Validate
    .exceptionally(ex -> defaultUser());                 // Error handling

// Combine two futures
CompletableFuture<Order> orderFuture = getOrder(orderId);
CompletableFuture<User> userFuture   = getUser(userId);

CompletableFuture<Receipt> receipt = orderFuture
    .thenCombine(userFuture, (order, user) -> createReceipt(order, user));

// Run after both complete
CompletableFuture.allOf(orderFuture, userFuture).thenRun(() -> {
    System.out.println("Both complete");
});

// Run after either completes
CompletableFuture.anyOf(source1, source2).thenAccept(result -> {
    System.out.println("First result: " + result);
});
```

---

<a name="volatile"></a>
# 12. VOLATILE — VISIBILITY GUARANTEE

## The Visibility Problem

```java
// WITHOUT volatile — BROKEN
class Worker {
    private boolean running = true;  // Cached in CPU register

    public void run() {
        while (running) {   // May read stale cached value!
            doWork();
        }
    }

    public void stop() {
        running = false;    // Writes to CPU cache, not RAM
        // Other thread's CPU cache still shows running = true!
    }
}
```

```java
// WITH volatile — FIXED
class Worker {
    private volatile boolean running = true;  // Always read from/write to RAM

    public void run() {
        while (running) {   // Always reads latest value
            doWork();
        }
    }

    public void stop() {
        running = false;    // Immediately visible to all threads
    }
}
```

## What Volatile Guarantees

```
✅ VISIBILITY: Writes immediately visible to all threads
✅ ORDERING: No reordering around volatile reads/writes
✅ 64-bit atomicity: long/double reads are atomic

❌ NOT ATOMIC for compound operations:
   count++  →  read-increment-write  →  NOT safe with just volatile
   Use AtomicInteger for this
```

```java
// Safe uses of volatile
private volatile boolean flag = false;         // Simple flag
private volatile int value = 0;                // Simple int assignment
private volatile User user = null;             // Reference assignment

// NOT safe with volatile alone
private volatile int counter = 0;
counter++;  // Still a race condition! Use AtomicInteger
```

## Volatile vs Synchronized vs Atomic

```
volatile:
  - Visibility only
  - Cheapest
  - No atomicity for compound ops
  - Use: flags, status, single assignment

AtomicInteger/Long/etc:
  - Visibility + atomicity for one variable
  - Lock-free (CAS)
  - Use: counters, accumulators

synchronized/Lock:
  - Visibility + atomicity for any block
  - Most powerful, most overhead
  - Use: compound operations on multiple variables
```

---

<a name="deadlocks"></a>
# 13. DEADLOCKS — DETECTION AND PREVENTION

## What is a Deadlock?

```
Thread A holds Lock 1, waits for Lock 2
Thread B holds Lock 2, waits for Lock 1
Both wait forever → program hangs
```

## Creating a Deadlock (What NOT to Do)

```java
Object lock1 = new Object();
Object lock2 = new Object();

// Thread A
new Thread(() -> {
    synchronized (lock1) {
        Thread.sleep(100);
        synchronized (lock2) { }  // Waits for lock2
    }
}).start();

// Thread B
new Thread(() -> {
    synchronized (lock2) {
        Thread.sleep(100);
        synchronized (lock1) { }  // Waits for lock1
    }
}).start();

// DEADLOCK! A waits for B, B waits for A
```

## Prevention Strategy 1 — Lock Ordering

```java
// ALWAYS acquire locks in same order
// If every thread locks in order: lock1 → lock2
// Deadlock is impossible

public void transfer(Account from, Account to, int amount) {
    // Order by account ID — consistent ordering
    Account first  = from.getId() < to.getId() ? from : to;
    Account second = from.getId() < to.getId() ? to : from;

    synchronized (first) {
        synchronized (second) {
            from.debit(amount);
            to.credit(amount);
        }
    }
}
```

## Prevention Strategy 2 — tryLock with Timeout

```java
public boolean transfer(Account from, Account to, int amount) {
    while (true) {
        boolean gotFrom = from.lock.tryLock(100, TimeUnit.MILLISECONDS);
        if (!gotFrom) continue;  // Retry

        try {
            boolean gotTo = to.lock.tryLock(100, TimeUnit.MILLISECONDS);
            if (!gotTo) continue;  // Release from, retry

            try {
                from.debit(amount);
                to.credit(amount);
                return true;
            } finally {
                to.lock.unlock();
            }
        } finally {
            from.lock.unlock();
        }
    }
}
```

## Prevention Strategy 3 — Reduce Lock Scope

```java
// BAD — holds lock during I/O
public synchronized void processAndSave(Data data) {
    process(data);
    database.save(data);  // I/O while holding lock!
    notify(data);         // Network while holding lock!
}

// GOOD — lock only what needs protection
public void processAndSave(Data data) {
    Data processed = process(data);   // No lock needed

    synchronized (this) {
        cache.put(data.getId(), processed);  // Lock only for shared state
    }

    database.save(processed);  // No lock — database handles its own concurrency
    notify(processed);         // No lock needed
}
```

## Detecting Deadlocks in Production

```java
// Using ThreadMXBean
ThreadMXBean bean = ManagementFactory.getThreadMXBean();
long[] deadlockedThreads = bean.findDeadlockedThreads();

if (deadlockedThreads != null) {
    ThreadInfo[] infos = bean.getThreadInfo(deadlockedThreads, true, true);
    for (ThreadInfo info : infos) {
        System.out.println("Deadlocked: " + info.getThreadName());
        System.out.println("Waiting for: " + info.getLockName());
        System.out.println("Held by: " + info.getLockOwnerName());
    }
}
```

---

<a name="virtual-threads"></a>
# 14. VIRTUAL THREADS (Java 19-21+)

## Platform vs Virtual Threads

```
Platform Thread:
  - One-to-one with OS thread
  - Heavy: ~1MB stack
  - Max ~thousands per JVM
  - I/O blocks the OS thread

Virtual Thread:
  - Many-to-few with carrier threads
  - Light: ~few KB
  - Max millions per JVM
  - I/O releases carrier thread
```

```java
// Platform thread — heavy
Thread platform = new Thread(() -> blockingIO());
platform.start();

// Virtual thread — lightweight
Thread virtual = Thread.ofVirtual().start(() -> blockingIO());

// Virtual thread executor — millions of tasks
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 0; i < 1_000_000; i++) {
        executor.submit(() -> {
            // Blocking I/O is fine!
            // When blocked, carrier thread is released for other virtual threads
            Connection conn = getDBConnection();
            ResultSet rs = conn.executeQuery("SELECT ...");
        });
    }
}  // Auto-shutdown

// Spring Boot 3.2+
// application.properties:
// spring.threads.virtual.enabled=true
// This makes Tomcat use virtual threads — massive throughput improvement
```

---

<a name="comparison"></a>
# 15. COMPLETE COMPARISON TABLE

## Lock Types — Choose the Right One

```
Scenario                              Best Choice
──────────────────────────────────────────────────────────────────────
Simple mutual exclusion               synchronized
Need timeout on acquire               ReentrantLock.tryLock()
Need to interrupt waiting thread      ReentrantLock.lockInterruptibly()
Need fair ordering (FIFO)             ReentrantLock(true) or Semaphore(n, true)
Many reads, few writes                ReentrantReadWriteLock
Mostly reads, rarely writes           StampedLock (optimistic)
Limit concurrent access to N          Semaphore(N)
Simple counter/flag                   AtomicInteger / AtomicBoolean / volatile
Compound operations on one variable   AtomicInteger (CAS)
Compound operations on multiple vars  ReentrantLock or synchronized
Producer-Consumer queue               BlockingQueue
Thread-safe Map                       ConcurrentHashMap
Thread-safe List (rare writes)        CopyOnWriteArrayList
Await specific condition              Condition (with ReentrantLock)
High-throughput I/O tasks             Virtual Threads (Java 21)
```

## Performance Ranking (Fastest → Slowest)

```
1. volatile              — just memory barrier, no lock
2. AtomicInteger (CAS)   — hardware instruction, no OS involvement
3. synchronized          — JVM-optimized, biased locking
4. ReentrantLock         — slightly more overhead than synchronized
5. ReadWriteLock         — overhead of two locks
6. Semaphore             — similar to ReentrantLock
7. StampedLock           — complex but optimistic reads are fastest
```

---

<a name="lld-application"></a>
# 16. LLD APPLICATION — WHERE TO USE WHAT

## Parking Lot System

```java
ParkingSlot.park()      → synchronized method
ParkingLot.activeTickets → ConcurrentHashMap
ticketCounter           → AtomicInteger
```

## LRU Cache

```java
get() → write lock (moves node — mutation)
put() → write lock
Ideally: ReentrantReadWriteLock
```

## Connection Pool

```java
borrow()             → Semaphore.tryAcquire(timeout)
returnConnection()   → Semaphore.release()
freeConnections      → LinkedBlockingQueue
activeConnections    → HashSet + ReentrantLock
```

## Notification System

```java
notificationQueue    → PriorityBlockingQueue (priority levels)
channelHandlers      → ConcurrentHashMap
retryCounter         → AtomicInteger
```

## Rate Limiter (Token Bucket)

```java
tokens              → AtomicInteger
lastRefillTime      → AtomicLong
perUserLimits       → ConcurrentHashMap<userId, AtomicInteger>
```

## Message Queue (Mini Kafka)

```java
topicQueues         → ConcurrentHashMap<topic, LinkedBlockingQueue>
offset tracking     → ConcurrentHashMap<consumerId, AtomicLong>
producer.send()     → No lock (BlockingQueue handles it)
```

---

# GOLDEN RULES — ALWAYS REMEMBER

```
1. Always release locks in finally block
   lock.lock();
   try { ... }
   finally { lock.unlock(); }

2. Never hold a lock during I/O operations
   Fetch data first, then lock for update

3. Acquire multiple locks in consistent order
   Prevents deadlock

4. Prefer tryLock over lock in production
   Never wait forever — always have a timeout

5. Use the right tool:
   Read-heavy → ReadWriteLock
   Bounded resource → Semaphore
   Counter → AtomicInteger
   Simple flag → volatile

6. synchronized is fine for simple cases
   Don't over-engineer with ReentrantLock

7. ConcurrentHashMap > Collections.synchronizedMap
   Far better throughput

8. BlockingQueue > manual wait/notify
   Cleaner, safer, correct

9. Virtual threads (Java 21) for I/O-heavy tasks
   Don't create platform threads per request

10. Test concurrent code with stress tests
    Race conditions don't always appear in unit tests
```

---

**This is your complete Java Concurrency reference.
Every LLD design, every interview question on threads —
this document has the answer.**
