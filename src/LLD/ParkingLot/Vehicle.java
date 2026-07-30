package LLD.ParkingLot;

import LLD.ParkingLot.ParkingLotConstants.VehicleType;
import LLD.ParkingLot.ParkingLotConstants.SlotType;

// Vehicle.java
public abstract class Vehicle {
    private final String licensePlate;
    private final VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() { return licensePlate; }
    public VehicleType getType()    { return type; }

    // Each vehicle knows what slot size it needs
    public abstract SlotType getRequiredSlotType();
}


