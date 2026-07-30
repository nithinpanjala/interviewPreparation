package LLD.ParkingLot;
import LLD.ParkingLot.ParkingLotConstants.*;

// Floor.java
import java.util.*;

public class Floor {
    private final int floorNumber;

    // Separate lists per slot type — O(1) availability check per type
    private final Map<SlotType, List<ParkingSlot>> slots;

    public Floor(int floorNumber, int smallSlots, int mediumSlots, int largeSlots) {
        this.floorNumber = floorNumber;
        this.slots = new HashMap<>();

        slots.put(SlotType.SMALL,  new ArrayList<>());
        slots.put(SlotType.MEDIUM, new ArrayList<>());
        slots.put(SlotType.LARGE,  new ArrayList<>());

        // Create slots with IDs like "F1-SM-01", "F1-MD-01"
        for (int i = 1; i <= smallSlots; i++) {
            String id = String.format("F%d-SM-%02d", floorNumber, i);
            slots.get(SlotType.SMALL).add(new ParkingSlot(id, SlotType.SMALL));
        }
        for (int i = 1; i <= mediumSlots; i++) {
            String id = String.format("F%d-MD-%02d", floorNumber, i);
            slots.get(SlotType.MEDIUM).add(new ParkingSlot(id, SlotType.MEDIUM));
        }
        for (int i = 1; i <= largeSlots; i++) {
            String id = String.format("F%d-LG-%02d", floorNumber, i);
            slots.get(SlotType.LARGE).add(new ParkingSlot(id, SlotType.LARGE));
        }
    }

    // Find first available slot of required type
    public Optional<ParkingSlot> findAvailableSlot(SlotType slotType) {
        return slots.get(slotType).stream()
                .filter(ParkingSlot::isAvailable)
                .findFirst();
    }

    // Count available slots of a type
    public long getAvailableCount(SlotType slotType) {
        return slots.get(slotType).stream()
                .filter(ParkingSlot::isAvailable)
                .count();
    }

    public int getFloorNumber() { return floorNumber; }

    public void displayAvailability() {
        System.out.printf("Floor %d → Small: %d | Medium: %d | Large: %d%n",
                floorNumber,
                getAvailableCount(SlotType.SMALL),
                getAvailableCount(SlotType.MEDIUM),
                getAvailableCount(SlotType.LARGE)
        );
    }
}