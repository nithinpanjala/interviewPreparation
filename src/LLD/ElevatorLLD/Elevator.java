package LLD.ElevatorLLD;

import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class Elevator {
    private final int id;
    private volatile int currentFloor;
    private volatile Direction currentDirection;

    // LOOK algorithm's core structure: stops still ahead in each direction, always sorted
    private final NavigableSet<Integer> upStops = new ConcurrentSkipListSet<>();
    private final NavigableSet<Integer> downStops = new ConcurrentSkipListSet<>();

    private final Object elevatorLock = new Object();   // per-elevator lock — same pattern as Connection Pool / Rate Limiter

    public Elevator(int id) {
        this.id = id;
        this.currentFloor = 0;
        this.currentDirection = Direction.IDLE;
    }

    // called for BOTH hall calls (after dispatch picks this elevator) and car calls
    public void addStop(int floor) {
        synchronized (elevatorLock) {
            if (floor > currentFloor) upStops.add(floor);
            else if (floor < currentFloor) downStops.add(floor);

            if (currentDirection == Direction.IDLE) {
                currentDirection = (floor >= currentFloor) ? Direction.UP : Direction.DOWN;
            }
        }
    }

    // one control-loop tick: move one floor, stop and open doors if this floor is a pending stop
    public void step() {
        synchronized (elevatorLock) {
            switch (currentDirection) {
                case UP:
                    if (!upStops.isEmpty()) {
                        int nextStop = upStops.first();          // closest pending stop above
                        moveTowards(nextStop);
                        if (currentFloor == nextStop) {
                            upStops.remove(nextStop);
                            openDoors();
                        }
                    } else if (!downStops.isEmpty()) {
                        currentDirection = Direction.DOWN;         // LOOK: reverse only when nothing left ahead
                    } else {
                        currentDirection = Direction.IDLE;
                    }
                    break;
                case DOWN:
                    if (!downStops.isEmpty()) {
                        int nextStop = downStops.last();          // closest pending stop below
                        moveTowards(nextStop);
                        if (currentFloor == nextStop) {
                            downStops.remove(nextStop);
                            openDoors();
                        }
                    } else if (!upStops.isEmpty()) {
                        currentDirection = Direction.UP;
                    } else {
                        currentDirection = Direction.IDLE;
                    }
                    break;
                default:
                    break;   // IDLE — nothing to do until a new stop arrives
            }
        }
    }

    private void moveTowards(int targetFloor) {
        if (currentFloor < targetFloor) currentFloor++;
        else if (currentFloor > targetFloor) currentFloor--;
    }

    private void openDoors() {
        System.out.println("Elevator " + id + " doors open at floor " + currentFloor);
    }

    // dispatch cost function: cheap if idle or already heading toward the request in the same direction
    public int costToServe(int requestedFloor, Direction requestedDirection) {
        synchronized (elevatorLock) {
            if (currentDirection == Direction.IDLE) {
                return Math.abs(currentFloor - requestedFloor);
            }
            boolean onTheWay = (currentDirection == requestedDirection) &&
                    ((currentDirection == Direction.UP && requestedFloor >= currentFloor) ||
                            (currentDirection == Direction.DOWN && requestedFloor <= currentFloor));
            return onTheWay ? Math.abs(currentFloor - requestedFloor) : Integer.MAX_VALUE / 2;
        }
    }

    public int getId() {
        return id;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    // add to Elevator — thread-safe read of total pending stops, used for load-based dispatch
    public int getPendingStopCount() {
        synchronized (elevatorLock) {
            return upStops.size() + downStops.size();
        }
    }
}
