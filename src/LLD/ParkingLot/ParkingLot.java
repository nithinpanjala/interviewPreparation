package LLD.ParkingLot;
import LLD.ParkingLot.ParkingLotConstants.*;

// ParkingLot.java
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class ParkingLot {

    // ── Singleton ─────────────────────────────────────────────────
    private static volatile ParkingLot instance;

    public static ParkingLot getInstance() {
        if (instance == null) {
            synchronized (ParkingLot.class) {
                if (instance == null) {
                    instance = new ParkingLot();
                }
            }
        }
        return instance;
    }

    // ── State ─────────────────────────────────────────────────────
    private final String name;
    private final List<Floor> floors;
    private final Map<String, Ticket> activeTickets;  // ticketId → Ticket
    private final FeeCalculator feeCalculator;
    private final AtomicInteger ticketCounter;

    private ParkingLot() {
        this.name           = "Central Parking";
        this.floors         = new ArrayList<>();
        this.activeTickets  = new ConcurrentHashMap<>();
        this.feeCalculator  = new HourlyFeeCalculator();  // Strategy injected
        this.ticketCounter  = new AtomicInteger(1000);
    }

    // ── Setup ─────────────────────────────────────────────────────
    public void addFloor(Floor floor) {
        floors.add(floor);
    }

    // ── Core Operations ───────────────────────────────────────────

    // PARK: find slot, issue ticket
    public Ticket park(Vehicle vehicle) {
        SlotType required = vehicle.getRequiredSlotType();

        // Search floors in order — first available slot
        for (Floor floor : floors) {
            Optional<ParkingSlot> slot = floor.findAvailableSlot(required);

            if (slot.isPresent()) {
                ParkingSlot parkingSlot = slot.get();

                boolean parked = parkingSlot.park(vehicle);
                if (!parked) continue;  // Concurrency: slot taken between find & park

                String ticketId = "TKT-" + ticketCounter.getAndIncrement();
                Ticket ticket   = new Ticket(ticketId, vehicle, parkingSlot);
                activeTickets.put(ticketId, ticket);

                System.out.printf("✅ Parked %s [%s] at slot %s | Ticket: %s%n",
                        vehicle.getLicensePlate(),
                        vehicle.getType(),
                        parkingSlot.getSlotId(),
                        ticketId
                );
                return ticket;
            }
        }

        System.out.printf("❌ No available slot for %s [%s]%n",
                vehicle.getLicensePlate(), vehicle.getType());
        return null;   // No slot available
    }

    // UNPARK: calculate fee, free slot, close ticket
    public double unpark(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);

        if (ticket == null) {
            throw new IllegalArgumentException("Ticket not found: " + ticketId);
        }

        // Calculate fee using strategy
        double fee = feeCalculator.calculate(ticket);
        ticket.closeTicket(fee);

        // Free the slot
        ticket.getSlot().unpark();

        // Remove from active tickets
        activeTickets.remove(ticketId);

        System.out.printf("🚗 Unparked %s | Duration: ... | Fee: ₹%.2f%n",
                ticket.getVehicle().getLicensePlate(), fee);
        System.out.println(ticket);

        return fee;
    }

    // ── Display ───────────────────────────────────────────────────
    public void displayAvailability() {
        System.out.println("═══ " + name + " — Availability ═══");
        floors.forEach(Floor::displayAvailability);
        System.out.println("════════════════════════════════════");
    }

    public boolean isFull(VehicleType vehicleType) {
        SlotType required = getSlotTypeFor(vehicleType);
        return floors.stream()
                .allMatch(f -> f.getAvailableCount(required) == 0);
    }

    private SlotType getSlotTypeFor(VehicleType type) {
        return switch (type) {
            case BIKE -> SlotType.SMALL;
            case CAR -> SlotType.MEDIUM;
            case TRUCK -> SlotType.LARGE;
            default -> throw new IllegalArgumentException("Unknown type");
        };
    }
}