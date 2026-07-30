package LLD.ElevatorLLD;

import java.util.List;

public class Zone {
    private final int minFloor;
    private final int maxFloor;
    private final List<Elevator> elevatorsInZone;

    public Zone(int minFloor, int maxFloor, List<Elevator> elevatorsInZone) {
        this.minFloor = minFloor;
        this.maxFloor = maxFloor;
        this.elevatorsInZone = elevatorsInZone;
    }

    public boolean containsFloor(int floor) {
        return floor >= minFloor && floor <= maxFloor;
    }

    public List<Elevator> getElevatorsInZone() {
        return elevatorsInZone;
    }
}
