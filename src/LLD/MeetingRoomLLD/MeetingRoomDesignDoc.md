**1. Concept from zero**

A meeting room booking system lets people reserve a room for a time interval, guaranteeing no two bookings for the same room ever overlap. Users search for a room matching criteria (capacity, floor, amenities) that's free for a given interval, then book it. Real-world: Outlook/Google Calendar room resources.

Why it's harder than it looks: the naive approach — "check if any existing booking overlaps, if not, create the booking" — has a classic check-then-act race. Two people can both check availability for Room A, 2–3pm, see it's free (neither has booked yet), and both proceed to book — resulting in a double-booked room. The check and the reservation must be one atomic operation, not two sequential steps.

**2. FR — derived from tracing a booking's lifecycle**

Someone wants a room for an interval, possibly with criteria (capacity, floor, projector) → system finds candidate rooms free for that whole interval matching criteria → user picks one, confirms → system must reserve it in a way that guarantees no conflict slipped in → later, view or cancel that booking.

1. `findAvailableRooms(interval, minCapacity, requiredAmenities)` — search candidates
2. `bookRoom(roomId, interval, organizer)` — reserve, must atomically fail on any conflict
3. Support many rooms, each with floor/capacity/amenities
4. `cancelBooking(roomId, bookingId)` — free the interval
5. Query a room's or organizer's upcoming bookings

**NFR — derived from concurrency/failure/resource-limit lenses**

1. **Atomicity of check-and-reserve** — the single most important guarantee; checking for conflict and creating the booking must be one indivisible operation
2. **Efficient conflict detection** — checking a new interval against every existing booking in a room is wasteful; needs to be faster than O(n) as bookings accumulate
3. **Concurrency scaling across rooms** — a lock covering the whole building would serialize unrelated bookings in different rooms for no reason
4. **Consistency on cancellation** — a freed slot must be immediately bookable by someone else, no stale state

**3. Design**

*Entities*
- `TimeInterval` — start, end (exclusive), `overlaps(other)`
- `Room` — id, floor, capacity, amenities
- `Booking` — id, room, interval, organizer
- `RoomSchedule` — per-room: a `TreeMap<startTime, Booking>` (bookings within one room are never overlapping, by invariant) + a per-room lock
- `MeetingRoomService` — orchestrator holding all rooms' schedules

*The key algorithmic insight*: because bookings *within a single room* are always non-overlapping (that invariant is enforced by the atomic check-and-reserve itself), a new request can only possibly conflict with its immediate neighbor before or after in start-time order — not with any other booking in the room. That turns conflict-checking into two `TreeMap` lookups (`floorEntry`/`ceilingEntry`), O(log n), instead of scanning every booking.

*Code*

```java
public class TimeInterval {
    private final long startTimeMillis;
    private final long endTimeMillis;   // exclusive

    public TimeInterval(long startTimeMillis, long endTimeMillis) {
        if (startTimeMillis >= endTimeMillis) throw new IllegalArgumentException("start must be before end");
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
    }

    public long getStartTimeMillis() { return startTimeMillis; }
    public long getEndTimeMillis() { return endTimeMillis; }

    public boolean overlaps(TimeInterval other) {
        return this.startTimeMillis < other.endTimeMillis && other.startTimeMillis < this.endTimeMillis;
    }
}

public class Room {
    private final String roomId;
    private final int floor;
    private final int capacity;
    private final Set<String> amenities;

    public Room(String roomId, int floor, int capacity, Set<String> amenities) {
        this.roomId = roomId;
        this.floor = floor;
        this.capacity = capacity;
        this.amenities = amenities;
    }

    public String getRoomId() { return roomId; }
    public int getFloor() { return floor; }
    public int getCapacity() { return capacity; }
    public Set<String> getAmenities() { return amenities; }
}

public class Booking {
    private final String bookingId;
    private final Room room;
    private final TimeInterval interval;
    private final String organizer;

    public Booking(String bookingId, Room room, TimeInterval interval, String organizer) {
        this.bookingId = bookingId;
        this.room = room;
        this.interval = interval;
        this.organizer = organizer;
    }

    public String getBookingId() { return bookingId; }
    public Room getRoom() { return room; }
    public TimeInterval getInterval() { return interval; }
    public String getOrganizer() { return organizer; }
}
```

```java
public class RoomSchedule {
    private final Room room;
    private final TreeMap<Long, Booking> bookingsByStartTime = new TreeMap<>();   // never-overlapping by invariant
    private final Object roomLock = new Object();   // per-room lock — other rooms never contend

    public RoomSchedule(Room room) {
        this.room = room;
    }

    // atomic check-and-reserve — the ONLY place double-booking is actually prevented
    public Booking tryBook(TimeInterval requestedInterval, String organizer) {
        synchronized (roomLock) {
            if (hasConflict(requestedInterval)) {
                return null;
            }
            Booking booking = new Booking(UUID.randomUUID().toString(), room, requestedInterval, organizer);
            bookingsByStartTime.put(requestedInterval.getStartTimeMillis(), booking);
            return booking;
        }
    }

    // O(log n): a new interval can only conflict with its immediate start-time neighbor before or after
    private boolean hasConflict(TimeInterval requestedInterval) {
        Map.Entry<Long, Booking> before = bookingsByStartTime.floorEntry(requestedInterval.getStartTimeMillis());
        if (before != null && before.getValue().getInterval().overlaps(requestedInterval)) return true;

        Map.Entry<Long, Booking> after = bookingsByStartTime.ceilingEntry(requestedInterval.getStartTimeMillis());
        return after != null && after.getValue().getInterval().overlaps(requestedInterval);
    }

    public boolean isAvailable(TimeInterval requestedInterval) {
        synchronized (roomLock) {
            return !hasConflict(requestedInterval);
        }
    }

    public boolean cancel(String bookingId) {
        synchronized (roomLock) {
            Iterator<Map.Entry<Long, Booking>> it = bookingsByStartTime.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getValue().getBookingId().equals(bookingId)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }
    }

    public Room getRoom() { return room; }
}

public class MeetingRoomService {
    private final Map<String, RoomSchedule> schedulesByRoomId = new ConcurrentHashMap<>();

    public void addRoom(Room room) {
        schedulesByRoomId.put(room.getRoomId(), new RoomSchedule(room));
    }

    public List<Room> findAvailableRooms(TimeInterval requestedInterval, int minCapacity, Set<String> requiredAmenities) {
        List<Room> available = new ArrayList<>();
        for (RoomSchedule schedule : schedulesByRoomId.values()) {
            Room room = schedule.getRoom();
            if (room.getCapacity() < minCapacity) continue;
            if (!room.getAmenities().containsAll(requiredAmenities)) continue;
            if (schedule.isAvailable(requestedInterval)) {
                available.add(room);
            }
        }
        return available;
    }

    public Booking bookRoom(String roomId, TimeInterval requestedInterval, String organizer) {
        RoomSchedule schedule = schedulesByRoomId.get(roomId);
        if (schedule == null) throw new IllegalArgumentException("Unknown room: " + roomId);

        Booking booking = schedule.tryBook(requestedInterval, organizer);
        if (booking == null) {
            throw new IllegalStateException("Room " + roomId + " is not available for the requested interval");
        }
        return booking;
    }

    public boolean cancelBooking(String roomId, String bookingId) {
        RoomSchedule schedule = schedulesByRoomId.get(roomId);
        return schedule != null && schedule.cancel(bookingId);
    }
}
```

*Thread safety — where and why*

Per-room lock (`synchronized(roomLock)`), not a global one — same recurring shape as Rate Limiter's per-bucket lock and Elevator's per-elevator lock; rooms are independent, serializing across all of them would be pure throughput loss. The critical correctness point: `hasConflict` and `bookingsByStartTime.put(...)` happen inside **one** synchronized block in `tryBook` — checking and reserving are one atomic step, which is exactly what a naive two-step "check, then book" implementation gets wrong. `TreeMap` itself isn't thread-safe, but since every access goes through `roomLock`, that's fine — same honest tradeoff discussed for Elevator's `TreeSet`-under-lock.

**Proactively flag this exact gap, it's the sharpest follow-up question here:** `findAvailableRooms` takes a snapshot — by the time a user picks a room from the results and calls `bookRoom`, someone else could have booked it in between. That's fine and expected — `findAvailableRooms` is for search/browse UX only; the actual safety guarantee lives entirely in `tryBook`'s atomic check-and-reserve inside `bookRoom`. If two users both try to book the same room found via search, exactly one succeeds and the other gets `IllegalStateException` — say this explicitly rather than let the interviewer think the search step is where safety comes from.

*Extensibility*
- Recurring meetings: a request expanding into multiple `TimeInterval`s, booked as one multi-interval transaction (discuss partial-failure semantics if asked — hard extension, good to flag as a talking point)
- Waitlisting on a full room, notify on cancellation — direct reuse of the Notification System's channel abstraction from earlier
- Ranking search results by best-fit capacity or floor proximity

*Interview walkthrough script*
1. "Per-room `TreeMap` of bookings keyed by start time — since bookings within a room are always non-overlapping, a new request only needs to check its immediate floor/ceiling neighbor — O(log n), not a full scan."
2. "Check-and-reserve is one atomic operation under a per-room lock — that's the actual double-booking prevention, not the search step."
3. "Per-room locking, not global, for the same reason as Rate Limiter and Elevator — independent resources shouldn't serialize each other."
4. "Search results can go stale before booking — that's expected, and the correctness guarantee lives in `tryBook`, not in `findAvailableRooms`."

*Connect to your experience*

The per-room lock guarding check-and-reserve is the same discipline as pessimistic row-level locking in your transaction platform — acquire the lock on the resource before checking state and mutating it, exactly like preventing a double-spend on a wallet balance.

Your turn — explain it back, or code it cold.