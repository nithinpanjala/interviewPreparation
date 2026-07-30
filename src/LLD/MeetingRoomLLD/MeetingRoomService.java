package LLD.MeetingRoomLLD;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
