package LLD.RateLimiterLLD;

import java.util.concurrent.ConcurrentHashMap;

public class SlidingWindowCounterLimiter implements RateLimiter {

    private final long windowSizeMillis;
    private final int maxRequestsPerWindow;

    private static class DualWindowState {
        long currentWindowStartMillis;
        int previousWindowCount;   // total requests in the window just before this one
        int currentWindowCount;    // requests so far in the active window
    }

    private final ConcurrentHashMap<String, DualWindowState> clientStates = new ConcurrentHashMap<>();

    public SlidingWindowCounterLimiter(long windowSizeMillis, int maxRequestsPerWindow) {
        this.windowSizeMillis = windowSizeMillis;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
    }

    @Override
    public boolean allowRequest(String clientId) {
        DualWindowState state = clientStates.computeIfAbsent(clientId, id -> new DualWindowState());

        synchronized (state) {
            long now = System.currentTimeMillis();
            long windowStartForNow = (now / windowSizeMillis) * windowSizeMillis;

            if (windowStartForNow != state.currentWindowStartMillis) {
                // shift: "current" becomes "previous" — but ONLY if it's the immediately adjacent window.
                // if the client was idle for more than one full window, previous count must be 0, not stale data.
                boolean isAdjacentWindow = (windowStartForNow - state.currentWindowStartMillis) == windowSizeMillis;
                state.previousWindowCount =  isAdjacentWindow ? state.currentWindowCount : 0;
                state.currentWindowCount = 0;
                state.currentWindowStartMillis = windowStartForNow;
            }

            double elapsedInCurrentWindow = now - state.currentWindowStartMillis;
            double overlapFraction = (windowSizeMillis - elapsedInCurrentWindow) / (double) windowSizeMillis;

            double estimatedRequestsInSlidingWindow =
                    (state.previousWindowCount * overlapFraction) + state.currentWindowCount;

            if (estimatedRequestsInSlidingWindow < maxRequestsPerWindow) {
                state.currentWindowCount++;
                return true;
            }
            return false;
        }
    }
}