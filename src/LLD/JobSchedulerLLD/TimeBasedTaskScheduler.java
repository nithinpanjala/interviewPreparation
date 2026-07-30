package LLD.JobSchedulerLLD;

import java.util.UUID;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
