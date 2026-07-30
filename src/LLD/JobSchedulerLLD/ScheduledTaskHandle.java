package LLD.JobSchedulerLLD;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

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

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isPeriodic() {
        return periodMillis > 0;
    }

    public String getTaskId() {
        return taskId;
    }

    public Task getTask() {
        return task;
    }

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
