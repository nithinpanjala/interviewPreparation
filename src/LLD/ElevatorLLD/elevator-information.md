Progress: Parking Lot (done earlier), Connection Pool (done), Rate Limiter (done). Next up: **Elevator System**.

**1. Concept from zero**

An elevator system serves people distributed across floors, each wanting to move up or down, using one or more elevator cars. Two kinds of requests exist: a **hall call** — someone standing on a floor presses up/down (they haven't picked an elevator yet, the system must assign one) — and a **car call** — someone already inside an elevator presses a destination floor button.

Why this is hard: if you just serve requests FIFO, an elevator zigzags — up 2 floors, down 1, up 5 — which is slow and unfair to whoever's waiting on floor 8 while the elevator keeps detouring elsewhere. The real solution is the same algorithm used for hard-disk scan scheduling, called **LOOK/SCAN**: an elevator commits to its current direction, serves every pending stop *ahead* of it in that direction, and only reverses once there's nothing left ahead. This is the one specific insight interviewers are checking for — if you don't mention it, they'll prompt for it.

**2. FR — derived from tracing the request lifecycle**

Person presses hall call (floor + direction) → some elevator must be chosen to answer it → elevator arrives → person boards, presses a car call (destination floor) → elevator must merge this new stop into whatever it's already committed to serving → elevator moves floor-by-floor, opening doors at each stop → repeat, indefinitely, with new hall/car calls arriving anytime mid-motion.

1. Support N elevators across floors 1..M
2. Accept hall calls: floor + direction
3. Accept car calls: destination floor from inside a specific elevator
4. Dispatch — pick the "best" elevator for a given hall call
5. Each elevator serves its stops using LOOK: continue current direction, pick up everything ahead, reverse only when empty ahead
6. Open/close doors at each stop
7. Handle new requests arriving *while already in motion* — dynamically insert into the existing schedule, don't require a full stop

**NFR — derived from concurrency/failure/resource-limit lenses**

1. **Thread-safety** — hall calls, car calls, and the elevator's own movement loop all mutate the same elevator state concurrently
2. **No starvation** — LOOK must guarantee every pending stop eventually gets served, never indefinitely skipped by direction reversal
3. **Fast dispatch decision** — picking an elevator for a hall call must be cheap even with many elevators/floors
4. **Extensible dispatch policy** — nearest-elevator vs least-busy vs zone-based must be swappable without touching elevator internals
5. **Fault tolerance** — an elevator taken out of service must have its pending stops reassigned, not silently dropped

**3. Design**

*Entities*
- `Direction` enum — UP, DOWN, IDLE
- `HallCall` — floor + direction
- `Elevator` — id, currentFloor, currentDirection, two sorted sets of pending stops (`upStops`, `downStops`) — this is the LOOK data structure
- `DispatchStrategy` (interface) — `selectElevator(elevators, hallCall)`
- `ElevatorController` — owns all elevators + a `DispatchStrategy`, exposes `requestElevator()` and `selectFloor()`

*Patterns*
- **Strategy** — `DispatchStrategy` interface, swap `NearestElevatorDispatchStrategy` for zone-based or least-busy without touching `Elevator`
- (Optional, mention if asked) **State** — elevator's IDLE/MOVING_UP/MOVING_DOWN/DOOR_OPEN could be a formal State pattern; a plain enum + switch is fine given interview time constraints — say this explicitly, it shows you know the tradeoff rather than over-engineering

*Code*

```java
public enum Direction { UP, DOWN, IDLE }

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
                        if (currentFloor == nextStop) { upStops.remove(nextStop); openDoors(); }
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
                        if (currentFloor == nextStop) { downStops.remove(nextStop); openDoors(); }
                    } else if (!upStops.isEmpty()) {
                        currentDirection = Direction.UP;
                    } else {
                        currentDirection = Direction.IDLE;
                    }
                    break;
                default: break;   // IDLE — nothing to do until a new stop arrives
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

    public int getId() { return id; }
    public int getCurrentFloor() { return currentFloor; }
}

public interface DispatchStrategy {
    Elevator selectElevator(List<Elevator> elevators, HallCall request);
}

public class NearestElevatorDispatchStrategy implements DispatchStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, HallCall request) {
        Elevator best = null;
        int bestCost = Integer.MAX_VALUE;
        for (Elevator elevator : elevators) {
            int cost = elevator.costToServe(request.getFloor(), request.getDirection());
            if (cost < bestCost) { bestCost = cost; best = elevator; }
        }
        return best;
    }
}

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
```

*Thread safety — where and why*

Third time this exact shape has shown up: **per-key lock, never a global one** — `synchronized(elevatorLock)` per elevator, same reasoning as the per-bucket lock in Rate Limiter and the pool-wide `ReentrantLock` in Connection Pool (there it's global because release/shutdown genuinely need pool-wide atomicity; here each elevator is independent, so a global lock would be pure throughput loss). `ConcurrentSkipListSet` for `upStops`/`downStops` gives safe concurrent add (new hall/car call) while `step()` reads `first()`/`last()`. `volatile` on `currentFloor`/`currentDirection` lets other components (a floor-display UI) read state without contending for the lock.

Be upfront about a real gap if asked: `costToServe` is evaluated per-elevator independently, so two hall calls arriving at the same instant could both select the same "best" elevator — there's a TOCTOU race across elevators during dispatch. Production systems either accept this (self-corrects next tick) or wrap dispatch selection in one global lock at a throughput cost. Naming this unprompted is a strong signal.

*Extensibility*
- Swap `NearestElevatorDispatchStrategy` for zone-based or least-busy — `ElevatorController` and `Elevator` untouched
- Capacity limits: reject a car call if at max weight, extend `addStop`
- Fire/emergency mode: a flag on `ElevatorController` that overrides dispatch to send all elevators to ground

*Interview walkthrough script*
1. "Two request types: hall calls need dispatch, car calls don't. Core algorithm is LOOK — commit to direction, serve everything ahead, reverse only when empty ahead, same as disk-arm scheduling."
2. "Per elevator, two sorted sets — stops ahead when going up, stops ahead when going down — `step()` always looks at the nearest one in the current direction."
3. "Dispatch is a cost function per elevator: cheap if idle or already heading toward the request in the right direction, else heavily penalized — wrapped in a Strategy so the policy is swappable."
4. "Concurrency: per-elevator lock, not global — elevators are independent, no reason to serialize across them."
5. Proactively flag the cross-elevator dispatch race as a known simplification.

*Connect to your experience*

The dispatch cost function is structurally the same problem as backend selection in a load balancer — "which of N workers is cheapest to route this request to right now" — directly analogous to routing decisions in your high-throughput transaction platform. The Strategy-pattern swap is the same shape as your interceptor/rule-engine pipeline: policy is pluggable, core mechanism isn't.

Both drop-in for `dispatchStrategy` in `ElevatorController` — no changes needed elsewhere, that's the whole point of Strategy.

First, one addition to `Elevator` both strategies need:

```java
// add to Elevator — thread-safe read of total pending stops, used for load-based dispatch
public int getPendingStopCount() {
    synchronized (elevatorLock) {
        return upStops.size() + downStops.size();
    }
}
```

**Least Busy** — picks the elevator carrying the fewest pending stops, among those actually heading toward the request. Fixes Nearest-Elevator's failure mode: always picking by distance can repeatedly overload one elevator that happens to sit close to a busy floor, while others idle.

```java
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
```

**Zone-Based** — building split into static floor ranges, each range served by a fixed subset of elevators (common in tall towers to bound worst-case travel time and give predictable lobby-rush behavior). Within a zone's candidates, delegate to another `DispatchStrategy` as a tie-break — Strategy composing Strategy.

```java
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

public class ZoneBasedDispatchStrategy implements DispatchStrategy {

    private final List<Zone> zones;                     // non-overlapping floor ranges covering the building
    private final DispatchStrategy tieBreakStrategy;     // e.g. NearestElevatorDispatchStrategy, reused within a zone

    public ZoneBasedDispatchStrategy(List<Zone> zones, DispatchStrategy tieBreakStrategy) {
        this.zones = zones;
        this.tieBreakStrategy = tieBreakStrategy;
    }

    @Override
    public Elevator selectElevator(List<Elevator> elevators, HallCall request) {
        for (Zone zone : zones) {
            if (zone.containsFloor(request.getFloor())) {
                List<Elevator> candidates = zone.getElevatorsInZone();
                if (!candidates.isEmpty()) {
                    return tieBreakStrategy.selectElevator(candidates, request);   // narrow to zone, then tie-break
                }
            }
        }
        // requested floor isn't covered by any configured zone — fall back to considering everyone
        return tieBreakStrategy.selectElevator(elevators, request);
    }
}
```

Say this line in the interview if you use Zone-Based: "this composes with the other strategies rather than replacing them — the zone narrows the candidate list, then any other `DispatchStrategy` picks the winner within it." That composition is a stronger signal than three independent, unrelated strategy classes.


**Your recap is accurate**, including one specifically good call: rejecting the State pattern here. You're right — State pattern earns its complexity when transitions have real behavior differences per state (entry/exit actions, guards, varying logic per state). Here it's just three labels driving a `switch` in one method — using State pattern would be pattern-for-pattern's-sake. Knowing when *not* to apply a pattern is worth saying out loud in the interview.

**1. Why `NavigableSet`, not plain `Set`?**

`Set` (the base interface, e.g. `HashSet`) guarantees *no* ordering — no `first()`, no `last()`, iteration order is undefined. `NavigableSet` is a stronger sub-interface (via `SortedSet`) that guarantees elements are maintained in sorted order and adds navigation methods: `first()`, `last()`, `higher(e)`, `lower(e)`, `ceiling(e)`, `floor(e)`, `pollFirst()`, `pollLast()`, `headSet()`, `tailSet()`.

We need exactly this because `upStops` only ever contains floors *above* the current floor — so `first()` (smallest element) is by definition the nearest stop above current. `downStops` only ever contains floors *below* current — so `last()` (largest element) is the nearest stop below current. Without ordering, you'd have to scan and find the min/max yourself every single `step()` call — O(n) per tick instead of O(log n) (tree) or effectively O(1) amortized for repeated first/last access.

**2. "It's a Set, how does first/last work?"**

Your confusion is conflating the *base* `Set` contract with the *`NavigableSet`* contract. `Set` itself promises nothing about order. But `NavigableSet extends SortedSet extends Set` — it's a subtype that adds a stronger guarantee on top: sorted order is part of its contract, so `first()`/`last()` are well-defined and meaningful. We never use plain `Set` anywhere in this design — we specifically declared the field type as `NavigableSet<Integer>`, which is what makes `first()`/`last()` legal and correct.

**3. `TreeSet` vs `ConcurrentSkipListSet` — and an honest gap in what I gave you**

`TreeSet` is the standard `NavigableSet` implementation — backed by a red-black tree, O(log n) add/remove/first/last, but **not thread-safe**. Concurrent add/remove from multiple threads can corrupt the tree or throw `ConcurrentModificationException`.

`ConcurrentSkipListSet` is a thread-safe `NavigableSet`, backed by a skip list (layered linked lists, probabilistic balancing) instead of a tree — same O(log n) expected complexity, safe for lock-free concurrent add/remove/first/last.

Here's the honest part, and a good thing to raise proactively if an interviewer pushes: **since every access to `upStops`/`downStops` in this design already happens inside `synchronized(elevatorLock)`, a plain `TreeSet` would actually be sufficient** — the coarse-grained lock already serializes all access, making `ConcurrentSkipListSet`'s own internal thread-safety redundant here. I'd defend `ConcurrentSkipListSet` as defense-in-depth: if a future feature adds a read (e.g. a monitoring/UI thread peeking at pending stops) that bypasses `elevatorLock` for a "cheap read," `TreeSet` would silently become unsafe the moment that happens, while `ConcurrentSkipListSet` stays safe on its own. If the interviewer asks "why not just TreeSet since you're already locking?" — the correct answer is "you're right, it's technically redundant under the current lock discipline; I'd default to TreeSet for simplicity and only reach for ConcurrentSkipListSet if I expect lock-free reads later." Saying this shows judgment, not just tool-recitation.

**4. Plain `Object` + `synchronized` vs `ReentrantLock`**

`synchronized` on an intrinsic monitor is simpler: the JVM guarantees the lock is released on exit from the block, even via an exception — no `finally` needed, no risk of forgetting to unlock. `ReentrantLock` earns its extra ceremony only when you need capabilities `synchronized` doesn't have: `tryLock(timeout)`, interruptible acquisition, a fairness policy, multiple `Condition` objects for fine-grained wait/notify, or holding the lock across a boundary that isn't a single contiguous block.

Here in `Elevator`, every critical section is fully contained inside one method with no timeout, no fairness need, no multiple wait-conditions — `synchronized` is the simplest tool that does the job, so that's what I used.

For honesty: the `ReentrantLock` in Connection Pool's `releaseConnection`/`shutdown` also didn't use any of `ReentrantLock`'s exclusive features (no `tryLock`, no `Condition`) — `synchronized` would have worked identically there too. If asked "why ReentrantLock there but synchronized here," the accurate answer is: neither case strictly *required* `ReentrantLock`'s extra capability; it's mostly a style choice, and I'd only insist on `ReentrantLock` if the design needed timeout-bounded lock acquisition or multiple conditions.

Why a dedicated `new Object()` rather than `synchronized(this)`: never synchronize on a publicly reachable object, including the instance itself. If any external code ever does `synchronized(elevatorInstance)` for an unrelated reason, it silently shares your monitor and can cause unexpected contention or deadlock. A `private final Object` used exclusively as a lock is encapsulated — nothing outside the class can ever accidentally acquire it. This is the standard "private lock object" idiom, worth naming explicitly if asked why you didn't just lock on `this`.

