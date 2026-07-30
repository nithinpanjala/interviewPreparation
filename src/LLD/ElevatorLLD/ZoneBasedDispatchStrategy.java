package LLD.ElevatorLLD;

import java.util.List;

public class ZoneBasedDispatchStrategy implements DispatchStrategy {

    private final List<Zone> zones;                     // non-overlapping floor ranges covering the building
    private final DispatchStrategy tieBreakStrategy;     // e.g. NearestElevatorDispatchStrategy, reused within a zone

    public ZoneBasedDispatchStrategy(List<Zone> zones, DispatchStrategy tieBreakStrategy) {
        this.zones = zones;
        this.tieBreakStrategy = tieBreakStrategy;
    }

    @Override
    public Elevator selectElevator(List<Elevator> elevators, HallCall request) {
        for (Zone zone : zones) {
            if (zone.containsFloor(request.getFloor())) {
                List<Elevator> candidates = zone.getElevatorsInZone();
                if (!candidates.isEmpty()) {
                    return tieBreakStrategy.selectElevator(candidates, request);   // narrow to zone, then tie-break
                }
            }
        }
        // requested floor isn't covered by any configured zone — fall back to considering everyone
        return tieBreakStrategy.selectElevator(elevators, request);
    }
}
