package LLD.MeetingRoomLLD;

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


