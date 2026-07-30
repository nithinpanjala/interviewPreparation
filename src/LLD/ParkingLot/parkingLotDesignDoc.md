
## FUNCTIONAL REQUIREMENTS

✅ Support multiple floors
✅ Support multiple vehicle types — Bike, Car, Truck
✅ Each vehicle type needs a specific slot size
Bike  → Small slot
Car   → Medium slot
Truck → Large slot
✅ Park a vehicle — find nearest available slot
✅ Unpark a vehicle — free the slot
✅ Generate a ticket on entry with timestamp
✅ Calculate fee on exit based on duration
✅ Display available slots per floor per type

## NON FUNCTIONAL REQUIREMENTS

✅ Thread-safe — multiple entry/exit gates operating simultaneously
✅ Extensible — easy to add new vehicle types, pricing strategies
✅ Scalable — works for 1 floor or 100 floors

## CONSTRAINTS

✅ A slot can hold exactly one vehicle
✅ A vehicle can only be parked once at a time
✅ Fee calculated per hour
✅ Different pricing per vehicle type

## IDENTIFY CLASSES

Reading the requirements, these nouns become classes:

ParkingLot      → the entire system (Singleton)

Floor           → one level of the parking lot

ParkingSlot     → one individual slot

Vehicle         → what gets parked (abstract)
├── Bike
├── Car
└── Truck

Ticket          → issued on entry

Fee Calculator  → calculates cost on exit (Strategy pattern)

Gate            → entry/exit point


## CLASS RELATIONSHIP 

ParkingLot
└── has many → Floor
                └── has many → ParkingSlot
                                      └── holds one → Vehicle (when occupied)

Vehicle (abstract)
├── Bike     → needs SlotType.SMALL
├── Car      → needs SlotType.MEDIUM
└── Truck    → needs SlotType.LARGE

Ticket
├── vehicle
├── slot
├── entryTime
└── exitTime

FeeCalculator (interface)
├── HourlyFeeCalculator
└── FlatRateFeeCalculator

## DESIGN PATTERNS USED 

Pattern          Where                        Why
──────────────────────────────────────────────────────────
Singleton      → ParkingLot                  Only one lot exists
Factory        → VehicleFactory              Create vehicle by type string
Strategy       → FeeCalculator               Swap pricing without code change
Observer       → ParkingLot events           Notify on full/available (extensible)

## THREAD SAFETY WHERE AND WHY

// ParkingSlot.park() is synchronized
// Prevents two threads parking in same slot simultaneously

public synchronized boolean park(Vehicle vehicle) {
if (!isAvailable) return false;  // double-check after acquiring lock
...
}

// ParkingLot uses ConcurrentHashMap for activeTickets
// Multiple gates can issue/close tickets simultaneously

private final Map<String, Ticket> activeTickets = new ConcurrentHashMap<>();

// AtomicInteger for ticket IDs
// Guarantees unique ticket numbers under concurrent access

private final AtomicInteger ticketCounter = new AtomicInteger(1000);

// Singleton uses double-checked locking
private static volatile ParkingLot instance;


## EXTENSIBILITY POINTS 

Add new vehicle type?
→ New class extends Vehicle
→ Add to VehicleFactory switch
→ Add slot type mapping
→ Zero changes to Floor or ParkingLot

Change pricing model?
→ New class implements FeeCalculator
→ Inject into ParkingLot constructor
→ Zero changes to existing code

Add reservation system?
→ Add ReservedSlot state to ParkingSlot
→ Add ReservationService
→ ParkingLot delegates reservation logic

Add multiple parking lots?
→ Remove Singleton
→ ParkingLotManager manages multiple lots
→ Find nearest available lot across locations


## Interview Walk-Through Script

"I'll start with requirements. We need to support multiple floors,
multiple vehicle types — bike, car, truck — each needing a different slot size. 
Core operations are park and unpark. 
We generate a ticket on entry and calculate fee on exit.


For design — 
ParkingLot is a Singleton since there's one lot.
It contains Floors, each Floor contains ParkingSlots organised by type. 
Vehicle is abstract with Bike, Car, Truck subclasses — each knows its required slot type. 
I use a Factory to create vehicles so callers don't depend on subclasses.


For pricing I use Strategy pattern — FeeCalculator is an interface. 
Today it's HourlyFeeCalculator, tomorrow I can swap in FlatRateFeeCalculator or
PeakHourCalculator with zero code change.

For thread safety — ParkingSlot.park() is synchronized to prevent double-booking.
ActiveTickets uses ConcurrentHashMap since multiple gates operate simultaneously. 
Ticket IDs use AtomicInteger for uniqueness.


This connects directly to what I built at TechMojo — the distributed transaction
platform has the same core concerns: concurrent access, atomic state changes,
and strategy-based processing rules."

## COMPLETE FILE STRUCTURE 
parking-lot/
├── enums/
│   ├── VehicleType.java
│   └── SlotType.java
├── vehicle/
│   ├── Vehicle.java
│   ├── Bike.java
│   ├── Car.java
│   ├── Truck.java
│   └── VehicleFactory.java
├── slot/
│   └── ParkingSlot.java
├── floor/
│   └── Floor.java
├── ticket/
│   └── Ticket.java
├── fee/
│   ├── FeeCalculator.java
│   ├── HourlyFeeCalculator.java
│   └── FlatRateFeeCalculator.java
├── lot/
│   └── ParkingLot.java
└── Main.java