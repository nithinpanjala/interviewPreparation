package LLD.ParkingLot;


// Bike.java
public class Bike extends Vehicle {
    public Bike(String licensePlate) {
        super(licensePlate, ParkingLotConstants.VehicleType.BIKE);
    }

    @Override
    public ParkingLotConstants.SlotType getRequiredSlotType() {
        return ParkingLotConstants.SlotType.SMALL;
    }
}