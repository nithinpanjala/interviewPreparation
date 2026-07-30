package LLD.RateLimiterLLD;


import java.util.concurrent.ConcurrentHashMap;

public class FixedWindowCounterLimiter implements RateLimiter {

    private final long windowSizeSec;      // e.g. 60_000 for a 1-minute window
    private final int maxRequestsPerWindow;   // e.g. 3

    // per-client state: which window we're currently counting, and how many requests so far
    private static class WindowState {
        long currentWindowStartSec;
        int requestCountInCurrentWindow;
    }

    private final ConcurrentHashMap<String, WindowState> clientWindows = new ConcurrentHashMap<>();

    public FixedWindowCounterLimiter(long windowSizeSec, int maxRequestsPerWindow) {
        this.windowSizeSec = windowSizeSec;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    public boolean allowRequest(String clientId) {
        WindowState state = clientWindows.computeIfAbsent(clientId, id -> new WindowState());

        synchronized (state) {   // per-client lock, not global — never serialize unrelated clients
            long now = System.currentTimeMillis();

            // snap "now" down to its window boundary
            long windowStartForNow = (now / windowSizeSec) * windowSizeSec;

            if (windowStartForNow != state.currentWindowStartSec) {
                // crossed into a new window — hard reset. This reset is the root cause of the boundary burst.
                state.currentWindowStartSec = windowStartForNow;
                state.requestCountInCurrentWindow = 0;
            }

            if (state.requestCountInCurrentWindow < maxRequestsPerWindow) {
                state.requestCountInCurrentWindow++;
                return true;
            }
            return false;
        }
    }
}
