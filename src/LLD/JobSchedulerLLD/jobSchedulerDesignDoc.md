**1. Concept from zero**

A task scheduler runs units of work at specific times or in a specific order — some tasks run once after a delay, some run repeatedly on a fixed cadence (cron-like), and some can only run after other tasks finish (a build pipeline where stage 2 waits on stage 1). This is fundamentally different from just calling `executorService.submit()`, because you need to track *when* something becomes eligible to run — a future time, or the completion of other tasks — before handing it to a worker thread.

Why it's two problems wearing one name: time-based scheduling (when?) and dependency-based scheduling (after what?) are genuinely different mechanisms, and a real system often needs both combined (e.g., "run daily, but only after yesterday's ETL job finished"). Most candidates only build the timer half and miss that dependency scheduling is really an event-driven topological sort.

**2. FR — derived from tracing how a task becomes eligible to run**

Someone schedules a task, either with a time trigger (once after a delay, or repeating every N seconds) or a dependency trigger (run after tasks X and Y complete) → the scheduler must efficiently know which task is next due, or reactively notice when a dependency finishes → hand the eligible task to a worker pool → for periodic tasks, determine the next occurrence after this run → for dependency-triggered tasks, when this one finishes, check if it unblocks anything waiting on it.

1. `scheduleOnce(task, delay)` — run once after a delay
2. `schedulePeriodic(task, initialDelay, period)` — run repeatedly at a fixed interval
3. `submitJob(job, dependencies)` — run once every listed dependency has completed
4. Efficient "what's next" lookup — not a full scan every tick
5. Concurrent execution via a bounded worker pool
6. Cancel a not-yet-run scheduled task
7. Detect dependency cycles before accepting a job

**NFR — derived from concurrency/failure/resource-limit lenses**

1. **Thread-safety** — scheduling, execution, and completion-triggered dispatch all happen concurrently from different threads
2. **Fault isolation** — one task's exception must never kill the dispatcher thread or block unrelated tasks
3. **Correctness under concurrent completions** — a "diamond" dependency (D depends on both B and C) must trigger D exactly once, not zero or twice, even if B and C finish at nearly the same instant
4. **Bounded resources** — a fixed worker pool, not unbounded thread creation per task
5. **No accidental cycles** — a submitted job with dependencies must be checked against the existing graph before acceptance

**3. Design**

*Entities*
- `Task` (interface) — `execute()`
- `ScheduledTaskHandle implements Delayed` — wraps a `Task` with next-run-time and period; feeds a `DelayQueue`
- `TimeBasedTaskScheduler` — dispatcher thread blocking on `DelayQueue.take()`, worker pool executes
- `Job` — id, `Task`, set of dependency job IDs
- `DependencyAwareJobScheduler` — in-degree counting per job (Kahn's algorithm, applied incrementally as completions happen rather than computed once upfront)

*Patterns*
- `Delayed` + `DelayQueue` — standard JDK building block for "wait until due," not a GoF pattern but worth naming precisely
- Dependency scheduling = **topological sort (Kahn's algorithm)**, reactive/event-driven instead of batch-computed — say this explicitly, it's the single most important insight for this half of the problem
- Completion triggering dependents is conceptually **Observer** — each job completion "notifies" its waiting dependents

*Code — time-based half*

```java
public interface Task {
    void execute() throws Exception;
}

public class ScheduledTaskHandle implements Delayed {
    private final String taskId;
    private final Task task;
    private final long periodMillis;          // 0 = one-time task
    private volatile long nextRunTimeMillis;
    private volatile boolean cancelled = false;

    public ScheduledTaskHandle(String taskId, Task task, long initialDelayMillis, long periodMillis) {
        this.taskId = taskId;
        this.task = task;
        this.periodMillis = periodMillis;
        this.nextRunTimeMillis = System.currentTimeMillis() + initialDelayMillis;
    }

    public void cancel() { cancelled = true; }
    public boolean isCancelled() { return cancelled; }
    public boolean isPeriodic() { return periodMillis > 0; }
    public String getTaskId() { return taskId; }
    public Task getTask() { return task; }

    public void rescheduleNext() {
        nextRunTimeMillis = System.currentTimeMillis() + periodMillis;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(nextRunTimeMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.nextRunTimeMillis, ((ScheduledTaskHandle) other).nextRunTimeMillis);
    }
}

public class TimeBasedTaskScheduler {
    private final DelayQueue<ScheduledTaskHandle> delayQueue = new DelayQueue<>();
    private final ExecutorService workerPool;
    private final Thread dispatcherThread;
    private volatile boolean running = true;

    public TimeBasedTaskScheduler(int workerPoolSize) {
        this.workerPool = Executors.newFixedThreadPool(workerPoolSize);
        this.dispatcherThread = new Thread(this::dispatchLoop);
        this.dispatcherThread.setDaemon(true);
        this.dispatcherThread.start();
    }

    public ScheduledTaskHandle scheduleOnce(Task task, long delayMillis) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle(UUID.randomUUID().toString(), task, delayMillis, 0);
        delayQueue.put(handle);
        return handle;
    }

    public ScheduledTaskHandle schedulePeriodic(Task task, long initialDelayMillis, long periodMillis) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle(UUID.randomUUID().toString(), task, initialDelayMillis, periodMillis);
        delayQueue.put(handle);
        return handle;
    }

    private void dispatchLoop() {
        while (running) {
            try {
                ScheduledTaskHandle handle = delayQueue.take();   // blocks until the earliest task is due
                if (handle.isCancelled()) continue;
                workerPool.submit(() -> runTaskSafely(handle));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void runTaskSafely(ScheduledTaskHandle handle) {
        try {
            handle.getTask().execute();
        } catch (Exception e) {
            System.err.println("Task " + handle.getTaskId() + " failed: " + e.getMessage());
        } finally {
            // rescheduling happens AFTER execution finishes — fixed-delay semantics, deliberately:
            // avoids two overlapping runs of the same periodic task if execution is slower than its period.
            // Rescheduling instead right after submit() (before execution completes) would be fixed-rate,
            // at the cost of possible overlap — call this tradeoff out if asked.
            if (handle.isPeriodic() && !handle.isCancelled()) {
                handle.rescheduleNext();
                delayQueue.put(handle);
            }
        }
    }

    public void shutdown() {
        running = false;
        dispatcherThread.interrupt();
        workerPool.shutdown();
    }
}
```

*Code — dependency-based half*

```java
public class Job {
    private final String jobId;
    private final Task task;
    private final Set<String> dependencyJobIds;

    public Job(String jobId, Task task, Set<String> dependencyJobIds) {
        this.jobId = jobId;
        this.task = task;
        this.dependencyJobIds = dependencyJobIds;
    }

    public String getJobId() { return jobId; }
    public Task getTask() { return task; }
    public Set<String> getDependencyJobIds() { return dependencyJobIds; }
}

public class DependencyAwareJobScheduler {
    private final ExecutorService workerPool;
    private final Map<String, Job> jobsById = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> dependentsOf = new ConcurrentHashMap<>();       // jobId -> jobs waiting on it
    private final Map<String, AtomicInteger> remainingDependencyCount = new ConcurrentHashMap<>();
    private final Set<String> completedJobIds = ConcurrentHashMap.newKeySet();
    private final Object graphLock = new Object();   // atomicity for cycle-check + graph registration

    public DependencyAwareJobScheduler(int workerPoolSize) {
        this.workerPool = Executors.newFixedThreadPool(workerPoolSize);
    }

    public void submitJob(Job job) {
        synchronized (graphLock) {
            if (wouldCreateCycle(job)) {
                throw new IllegalArgumentException("Job " + job.getJobId() + " would introduce a dependency cycle");
            }

            jobsById.put(job.getJobId(), job);
            int unmetDependencies = 0;
            for (String depId : job.getDependencyJobIds()) {
                if (completedJobIds.contains(depId)) continue;
                dependentsOf.computeIfAbsent(depId, id -> ConcurrentHashMap.newKeySet()).add(job.getJobId());
                unmetDependencies++;
            }
            remainingDependencyCount.put(job.getJobId(), new AtomicInteger(unmetDependencies));

            if (unmetDependencies == 0) {
                dispatch(job);
            }
        }
    }

    private void dispatch(Job job) {
        workerPool.submit(() -> {
            try {
                job.getTask().execute();
            } catch (Exception e) {
                System.err.println("Job " + job.getJobId() + " failed: " + e.getMessage());
            } finally {
                onJobCompleted(job.getJobId());
            }
        });
    }

    private void onJobCompleted(String jobId) {
        completedJobIds.add(jobId);
        Set<String> waitingJobs = dependentsOf.getOrDefault(jobId, Collections.emptySet());
        for (String dependentJobId : waitingJobs) {
            // atomic decrement — the diamond-dependency guarantee: whichever completing dependency's
            // decrement brings this to exactly zero is the one that dispatches; no double-dispatch, no miss
            int remaining = remainingDependencyCount.get(dependentJobId).decrementAndGet();
            if (remaining == 0) {
                dispatch(jobsById.get(dependentJobId));
            }
        }
    }

    private boolean wouldCreateCycle(Job job) {
        Set<String> visited = new HashSet<>();
        Deque<String> stack = new ArrayDeque<>(job.getDependencyJobIds());
        while (!stack.isEmpty()) {
            String current = stack.pop();
            if (current.equals(job.getJobId())) return true;
            if (!visited.add(current)) continue;
            Job currentJob = jobsById.get(current);
            if (currentJob != null) stack.addAll(currentJob.getDependencyJobIds());
        }
        return false;
    }
}
```

*Thread safety — where and why*

`DelayQueue` is itself thread-safe for concurrent `put`/`take`. `ConcurrentHashMap`/`ConcurrentHashMap.newKeySet()` for `jobsById`, `dependentsOf`, `completedJobIds` — multiple worker threads complete jobs concurrently, all calling `onJobCompleted` at once. `AtomicInteger.decrementAndGet()` is what makes the diamond case correct — without it, two threads reading-then-writing the same counter non-atomically could both see "1 remaining" and neither triggers the dependent, or both see it hit zero and dispatch it twice. `synchronized(graphLock)` around `submitJob`'s cycle-check-then-register — must be atomic, or two concurrent submissions could each pass a cycle check against a stale graph snapshot and jointly introduce a cycle that neither individually would have created.

*Extensibility*
- Retry per task: wrap `Task` in the same Decorator shape as `RetryingNotificationChannel` from Notification System — same pattern, different domain
- Combine both halves: a `Job` gains an optional time trigger alongside dependencies — dispatch when *either* condition is satisfied, or when *both* are (your call based on requirement)
- Persistence: back `jobsById`/`completedJobIds` with a DB so scheduled state survives a restart

*Interview walkthrough script*
1. "Two orthogonal triggers — time-based and dependency-based — I'll show both and how they'd merge."
2. "Time-based: `Delayed` + `DelayQueue`, dispatcher thread blocks on `take()`, hands due tasks to a worker pool."
3. "Periodic rescheduling happens in a `finally` block after execution — fixed-delay, deliberately, to avoid overlapping runs of a slow task."
4. "Dependency-based is topological sort, but incremental and event-driven — in-degree counting via `AtomicInteger`, a job dispatches the instant its count hits zero, and atomicity is what makes diamond dependencies correct."
5. "Cycle detection via DFS under a lock before accepting any new job."

*Connect to your experience*

The dependency-count-and-dispatch model is structurally your interceptor/rule-engine pipeline — steps only proceed once upstream steps clear. `ExecutorService` usage throughout is your listed strength directly applied. If asked about production hardening, mention Kafka: a job's completion event could be published to a topic, and dependents subscribe rather than living in an in-memory map — same decoupling idea as your event-driven microservices work.

Your turn — explain it back, or code it cold.