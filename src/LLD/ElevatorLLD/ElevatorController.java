package LLD.ElevatorLLD;

import java.util.List;

public class ElevatorController {
    private final List<Elevator> elevators;
    private final DispatchStrategy dispatchStrategy;

    public ElevatorController(List<Elevator> elevators, DispatchStrategy dispatchStrategy) {
        this.elevators = elevators;
        this.dispatchStrategy = dispatchStrategy;
    }

    public void requestElevator(int floor, Direction direction) {          // hall call
        Elevator chosen = dispatchStrategy.selectElevator(elevators, new HallCall(floor, direction));
        chosen.addStop(floor);
    }

    public void selectFloor(int elevatorId, int destinationFloor) {        // car call
        elevators.stream().filter(e -> e.getId() == elevatorId).findFirst()
                .ifPresent(e -> e.addStop(destinationFloor));
    }

    public void tick() {                                                    // driven by a scheduler thread
        elevators.forEach(Elevator::step);
    }
}
