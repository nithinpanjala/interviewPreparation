package LLD.MeetingRoomLLD;

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

