package LLD.ElevatorLLD;

import java.util.Comparator;
import java.util.List;

public class LeastBusyDispatchStrategy implements DispatchStrategy {

    @Override
    public Elevator selectElevator(List<Elevator> elevators, HallCall request) {
        Elevator best = null;
        int fewestPendingStops = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            // reuse the same cost function — skip elevators going the wrong way or already past the floor
            int cost = elevator.costToServe(request.getFloor(), request.getDirection());
            if (cost == Integer.MAX_VALUE / 2) continue;

            int pendingStops = elevator.getPendingStopCount();
            if (pendingStops < fewestPendingStops) {
                fewestPendingStops = pendingStops;
                best = elevator;
            }
        }

        // fallback: every elevator is currently "wrong way" — pick the least loaded overall rather than fail
        if (best == null) {
            best = elevators.stream()
                    .min(Comparator.comparingInt(Elevator::getPendingStopCount))
                    .orElseThrow(() -> new IllegalStateException("No elevators available"));
        }
        return best;
    }
}
