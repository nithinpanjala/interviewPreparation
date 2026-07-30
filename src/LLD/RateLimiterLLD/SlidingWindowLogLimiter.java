package LLD.RateLimiterLLD;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

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

