package LLD.ElevatorLLD;

import java.util.List;

public class NearestElevatorDispatchStrategy implements DispatchStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, HallCall request) {
        Elevator best = null;
        int bestCost = Integer.MAX_VALUE;
        for (Elevator elevator : elevators) {
            int cost = elevator.costToServe(request.getFloor(), request.getDirection());
            if (cost < bestCost) {
                bestCost = cost;
                best = elevator;
            }
        }
        return best;
    }
}
