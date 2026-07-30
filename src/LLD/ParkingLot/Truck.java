package LLD.ParkingLot;

// Truck.java
public class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, ParkingLotConstants.VehicleType.TRUCK);
    }

    @Override
    public ParkingLotConstants.SlotType getRequiredSlotType() {
        return ParkingLotConstants.SlotType.LARGE;
    }
}
