package LLD.JobSchedulerLLD;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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
