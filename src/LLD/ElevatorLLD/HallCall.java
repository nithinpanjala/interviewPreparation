package LLD.ElevatorLLD;

public class HallCall {
    private final int floor;
    private final Direction direction;

    public HallCall(int floor, Direction direction) {
        this.floor = floor;
        this.direction = direction;
    }

    public int getFloor() { return floor; }
    public Direction getDirection() { return direction; }
}

