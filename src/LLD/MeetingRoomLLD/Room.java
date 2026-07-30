package LLD.MeetingRoomLLD;

import java.util.Set;

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

    public String getRoomId() {
        return roomId;
    }

    public int getFloor() {
        return floor;
    }

    public int getCapacity() {
        return capacity;
    }

    public Set<String> getAmenities() {
        return amenities;
    }
}
