package LLD.ElevatorLLD;

import java.util.List;

public interface DispatchStrategy {
    Elevator selectElevator(List<Elevator> elevators, HallCall request);
}
