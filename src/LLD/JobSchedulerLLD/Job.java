package LLD.JobSchedulerLLD;

import java.util.Set;

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

