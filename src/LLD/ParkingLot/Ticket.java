package LLD.ParkingLot;

import java.time.LocalDateTime;

public class Ticket {
    private final String ticketId;
    private final Vehicle vehicle;
    private final ParkingSlot slot;
    private final LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double fee;

    public Ticket(String ticketId, Vehicle vehicle, ParkingSlot slot) {
        this.ticketId  = ticketId;
        this.vehicle   = vehicle;
        this.slot      = slot;
        this.entryTime = LocalDateTime.now();
    }

    // Called when vehicle exits
    public void closeTicket(double fee) {
        this.exitTime = LocalDateTime.now();
        this.fee      = fee;
    }

    public String getTicketId()         { return ticketId; }
    public Vehicle getVehicle()         { return vehicle; }
    public ParkingSlot getSlot()        { return slot; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime()  { return exitTime; }
    public double getFee()              { return fee; }

    @Override
    public String toString() {
        return String.format(
                "Ticket[%s] | Vehicle: %s | Slot: %s | Entry: %s | Exit: %s | Fee: %.2f",
                ticketId,
                vehicle.getLicensePlate(),
                slot.getSlotId(),
                entryTime,
                exitTime != null ? exitTime : "Still parked",
                fee
        );
    }
}