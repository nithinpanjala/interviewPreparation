package LLD.MeetingRoomLLD;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

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

    public Room getRoom() {
        return room;
    }
}
