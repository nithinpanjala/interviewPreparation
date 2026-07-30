package LLD.ParkingLot;

import LLD.ParkingLot.ParkingLotConstants.*;

// VehicleFactory.java
public class VehicleFactory {

    // Create vehicle without caller knowing the subclass
    public static Vehicle createVehicle(VehicleType type, String licensePlate) {
        return switch (type) {
            case BIKE -> new Bike(licensePlate);
            case CAR -> new Car(licensePlate);
            case TRUCK -> new Truck(licensePlate);
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + type);
        };
    }
}

// Usage
//Vehicle car = VehicleFactory.createVehicle(VehicleType.CAR, "KA-01-AB-1234");
