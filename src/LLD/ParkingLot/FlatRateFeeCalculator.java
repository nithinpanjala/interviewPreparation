package LLD.ParkingLot;
import LLD.ParkingLot.ParkingLotConstants.*;

import java.util.Map;

// FlatRateFeeCalculator.java — swap strategy without changing anything else
public class FlatRateFeeCalculator implements FeeCalculator {

    private static final Map<VehicleType, Double> FLAT_RATES = Map.of(
            VehicleType.BIKE,  50.0,
            VehicleType.CAR,   100.0,
            VehicleType.TRUCK, 200.0
    );

    @Override
    public double calculate(Ticket ticket) {
        return FLAT_RATES.get(ticket.getVehicle().getType());
    }
}