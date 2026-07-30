package LLD.ParkingLot;
import LLD.ParkingLot.ParkingLotConstants.*;

// HourlyFeeCalculator.java
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class HourlyFeeCalculator implements FeeCalculator {

    // Rates per vehicle type per hour
    private static final Map<VehicleType, Double> HOURLY_RATES = Map.of(
            VehicleType.BIKE,  20.0,
            VehicleType.CAR,   50.0,
            VehicleType.TRUCK, 100.0
    );

    @Override
    public double calculate(Ticket ticket) {
        LocalDateTime entry = ticket.getEntryTime();
        LocalDateTime exit  = LocalDateTime.now();

        // Duration in hours — minimum 1 hour
        long minutes = Duration.between(entry, exit).toMinutes();
        double hours = Math.max(1.0, Math.ceil(minutes / 60.0));

        double rate = HOURLY_RATES.get(ticket.getVehicle().getType());
        return hours * rate;
    }
}
