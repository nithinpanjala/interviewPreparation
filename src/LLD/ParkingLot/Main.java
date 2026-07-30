package LLD.ParkingLot;
import LLD.ParkingLot.ParkingLotConstants.*;

// Main.java
public class Main {

    public static void main(String[] args) throws InterruptedException {

        // Setup parking lot
        ParkingLot lot = ParkingLot.getInstance();

        // Add 2 floors
        // Floor 1: 5 small, 5 medium, 2 large slots
        lot.addFloor(new Floor(1, 5, 5, 2));
        // Floor 2: 3 small, 3 medium, 1 large slot
        lot.addFloor(new Floor(2, 3, 3, 1));

        lot.displayAvailability();

        // Create vehicles
        Vehicle bike1  = VehicleFactory.createVehicle(VehicleType.BIKE,  "KA-01-B-0001");
        Vehicle car1   = VehicleFactory.createVehicle(VehicleType.CAR,   "KA-01-C-1234");
        Vehicle car2   = VehicleFactory.createVehicle(VehicleType.CAR,   "KA-01-C-5678");
        Vehicle truck1 = VehicleFactory.createVehicle(VehicleType.TRUCK, "KA-01-T-9999");

        // Park vehicles
        Ticket t1 = lot.park(bike1);
        Ticket t2 = lot.park(car1);
        Ticket t3 = lot.park(car2);
        Ticket t4 = lot.park(truck1);

        lot.displayAvailability();

        // Simulate parking duration
        Thread.sleep(2000);

        // Unpark
        lot.unpark(t2.getTicketId());
        lot.unpark(t4.getTicketId());

        lot.displayAvailability();
    }
}