package LLD.ParkingLot;
import LLD.ParkingLot.ParkingLotConstants.*;


// ParkingSlot.java
public class ParkingSlot {
    private final String slotId;        // e.g. "F1-S-01" (Floor1, Small, slot01)
    private final SlotType slotType;
    private Vehicle parkedVehicle;      // null when empty
    private boolean isAvailable;

    public ParkingSlot(String slotId, SlotType slotType) {
        this.slotId       = slotId;
        this.slotType     = slotType;
        this.isAvailable  = true;
        this.parkedVehicle = null;
    }

    // Park a vehicle in this slot
    public synchronized boolean park(Vehicle vehicle) {
        if (!isAvailable) {
            return false;   // slot taken
        }
        this.parkedVehicle = vehicle;
        this.isAvailable   = false;
        return true;
    }

    // Remove vehicle from this slot
    public synchronized Vehicle unpark() {
        if (isAvailable) {
            return null;    // slot already empty
        }
        Vehicle vehicle    = this.parkedVehicle;
        this.parkedVehicle = null;
        this.isAvailable   = true;
        return vehicle;
    }

    public String getSlotId()         { return slotId; }
    public SlotType getSlotType()     { return slotType; }
    public boolean isAvailable()      { return isAvailable; }
    public Vehicle getParkedVehicle() { return parkedVehicle; }
}
