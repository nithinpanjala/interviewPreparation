package LLD.ParkingLot;

// Car.java
public class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, ParkingLotConstants.VehicleType.CAR);
    }

    @Override
    public ParkingLotConstants.SlotType getRequiredSlotType() {

        return ParkingLotConstants.SlotType.MEDIUM;
    }
}
