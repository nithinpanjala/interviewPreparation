Progress: Parking Lot, Connection Pool, Rate Limiter, Elevator System — done. LRU Cache is already solid from your DSA prep (DLL+HashMap, thread-safe). Skipping ahead to fresh ground: **Splitwise (Expense Sharing)**.

**1. Concept from zero**

A group shares expenses — one person pays for dinner, the system splits the cost among participants and tracks who owes whom. Over many expenses across many people, this becomes a tangled web of pairwise debts (A owes B, B owes C, C owes A). The system needs to calculate splits correctly, maintain running balances, and let people settle up.

Why it's more than "just subtract some numbers": splits aren't always equal — exact amounts, percentages, and equal splits all need different math, and money math has a rounding trap most candidates miss (split $10 three ways: $3.33 × 3 = $9.99, a cent vanishes). Also, at scale, the "who owes whom" graph can be simplified — if A owes B $10 and B owes C $10, that nets to A owing C $10 directly, cutting the number of actual payments needed. That simplification is a real graph/greedy problem, worth mentioning even if you don't have time to fully implement it.

**2. FR — derived from tracing an expense's lifecycle**

Someone pays for something on behalf of a group → system must calculate each participant's share, using whichever split rule was specified → each participant's balance with the payer must update → later, anyone should be able to query "who owes whom, how much" → eventually someone settles a debt, balance updates again → the whole thing must support different split rules without rewriting core logic.

1. `addExpense(payer, amount, participants, splitType, inputs)` — record an expense and compute shares
2. Support multiple split types: equal, exact amounts, percentage — pluggable
3. Maintain a running net balance between every pair of users
4. `settleUp(payer, payee, amount)` — record a payment, reduce the balance
5. `getBalance(userA, userB)` — query net balance between two users

**NFR — derived from concurrency/failure/precision lenses**

1. **Atomicity** — one expense touches N participants' balances at once; a partial update (some participants' balances updated, others not, due to a crash or race) corrupts the ledger permanently
2. **Precision** — money must never use `float`/`double`; binary floating point can't represent currency exactly, must use `BigDecimal`
3. **Thread-safety** — multiple group members can add expenses concurrently
4. **Extensibility** — new split types added without touching balance-tracking logic
5. **Correctness under rounding** — split calculations must account for leftover cents deterministically, never silently drop or invent money

**3. Design**

*Entities*
- `User` — id, name
- `Split` — user, amountOwed
- `SplitStrategy` (interface) — `calculateSplits(totalAmount, participants, inputValues)`
- `EqualSplitStrategy`, `ExactSplitStrategy`, `PercentageSplitStrategy` — implementations
- `Expense` — id, paidBy, totalAmount, list of `Split`
- `BalanceSheet` — nested map of net balances between user pairs
- `SplitwiseService` — orchestrator: registers strategies, creates expenses, applies to ledger

*Patterns*
- **Strategy** — `SplitStrategy` interface, one implementation per split type, service picks by key — new split type is a new class, zero changes elsewhere
- Not a Factory here — strategies are registered in a fixed map, not dynamically constructed per request; don't force a Factory in where a simple registry suffices

*Code*

```java
public class User {
    private final String userId;
    private final String name;

    public User(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public String getUserId() { return userId; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User)) return false;
        return userId.equals(((User) o).userId);
    }

    @Override
    public int hashCode() { return userId.hashCode(); }
}

public class Split {
    private final User user;
    private final BigDecimal amountOwed;

    public Split(User user, BigDecimal amountOwed) {
        this.user = user;
        this.amountOwed = amountOwed;
    }

    public User getUser() { return user; }
    public BigDecimal getAmountOwed() { return amountOwed; }
}

public interface SplitStrategy {
    // inputValues meaning depends on strategy: null for equal, exact amounts for Exact, percentages for Percentage
    List<Split> calculateSplits(BigDecimal totalAmount, List<User> participants, Map<User, BigDecimal> inputValues);
}
```

```java
public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount, List<User> participants, Map<User, BigDecimal> inputValues) {
        int participantCount = participants.size();
        BigDecimal baseShare = totalAmount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.FLOOR);
        BigDecimal totalAllocated = baseShare.multiply(BigDecimal.valueOf(participantCount));
        BigDecimal roundingRemainder = totalAmount.subtract(totalAllocated);  // leftover cents from flooring

        List<Split> splits = new ArrayList<>();
        for (int i = 0; i < participantCount; i++) {
            BigDecimal share = baseShare;
            if (i == 0) {
                share = share.add(roundingRemainder);   // deterministic: first participant absorbs the leftover cents
            }
            splits.add(new Split(participants.get(i), share));
        }
        return splits;
    }
}

public class ExactSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount, List<User> participants, Map<User, BigDecimal> inputValues) {
        BigDecimal sumOfExactAmounts = inputValues.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumOfExactAmounts.compareTo(totalAmount) != 0) {
            throw new IllegalArgumentException(
                "Exact amounts (" + sumOfExactAmounts + ") must sum to total (" + totalAmount + ")");
        }
        List<Split> splits = new ArrayList<>();
        for (User participant : participants) {
            splits.add(new Split(participant, inputValues.get(participant)));
        }
        return splits;
    }
}

public class PercentageSplitStrategy implements SplitStrategy {
    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount, List<User> participants, Map<User, BigDecimal> inputValues) {
        BigDecimal sumOfPercentages = inputValues.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sumOfPercentages.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new IllegalArgumentException("Percentages must sum to 100, got " + sumOfPercentages);
        }
        List<Split> splits = new ArrayList<>();
        for (User participant : participants) {
            BigDecimal percentage = inputValues.get(participant);
            BigDecimal share = totalAmount.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            splits.add(new Split(participant, share));
        }
        return splits;
    }
}
```

```java
public class Expense {
    private final String expenseId;
    private final User paidBy;
    private final BigDecimal totalAmount;
    private final List<Split> splits;

    public Expense(String expenseId, User paidBy, BigDecimal totalAmount, List<Split> splits) {
        this.expenseId = expenseId;
        this.paidBy = paidBy;
        this.totalAmount = totalAmount;
        this.splits = splits;
    }

    public User getPaidBy() { return paidBy; }
    public List<Split> getSplits() { return splits; }
}

public class BalanceSheet {
    // netBalances.get(A).get(B) = how much A owes B, before netting against the reverse direction
    private final Map<String, Map<String, BigDecimal>> netBalances = new ConcurrentHashMap<>();
    private final Object ledgerLock = new Object();   // guards atomicity of a whole expense's multi-user update

    public void applyExpense(Expense expense) {
        synchronized (ledgerLock) {   // one expense touches many participants — must apply all-or-nothing
            User payer = expense.getPaidBy();
            for (Split split : expense.getSplits()) {
                User participant = split.getUser();
                if (participant.equals(payer)) continue;   // payer doesn't owe themselves their own share
                adjustBalance(participant, payer, split.getAmountOwed());   // participant owes payer this amount
            }
        }
    }

    public void settleUp(User payer, User payee, BigDecimal amount) {
        synchronized (ledgerLock) {
            adjustBalance(payer, payee, amount.negate());   // reduces what payer owes payee
        }
    }

    private void adjustBalance(User fromUser, User toUser, BigDecimal amount) {
        netBalances.computeIfAbsent(fromUser.getUserId(), id -> new ConcurrentHashMap<>());
        Map<String, BigDecimal> fromOwesMap = netBalances.get(fromUser.getUserId());
        BigDecimal existing = fromOwesMap.getOrDefault(toUser.getUserId(), BigDecimal.ZERO);
        fromOwesMap.put(toUser.getUserId(), existing.add(amount));
    }

    public BigDecimal getNetBalance(User userA, User userB) {
        BigDecimal aOwesB = netBalances.getOrDefault(userA.getUserId(), Collections.emptyMap())
                .getOrDefault(userB.getUserId(), BigDecimal.ZERO);
        BigDecimal bOwesA = netBalances.getOrDefault(userB.getUserId(), Collections.emptyMap())
                .getOrDefault(userA.getUserId(), BigDecimal.ZERO);
        return aOwesB.subtract(bOwesA);   // positive => A owes B net; negative => B owes A net
    }
}

public class SplitwiseService {
    private final BalanceSheet balanceSheet = new BalanceSheet();
    private final Map<String, SplitStrategy> strategies = new ConcurrentHashMap<>();

    public SplitwiseService() {
        strategies.put("EQUAL", new EqualSplitStrategy());
        strategies.put("EXACT", new ExactSplitStrategy());
        strategies.put("PERCENTAGE", new PercentageSplitStrategy());
    }

    public Expense addExpense(User paidBy, BigDecimal amount, List<User> participants,
                               String splitType, Map<User, BigDecimal> inputValues) {
        SplitStrategy strategy = strategies.get(splitType);
        if (strategy == null) throw new IllegalArgumentException("Unknown split type: " + splitType);

        List<Split> splits = strategy.calculateSplits(amount, participants, inputValues);
        Expense expense = new Expense(UUID.randomUUID().toString(), paidBy, amount, splits);
        balanceSheet.applyExpense(expense);
        return expense;
    }

    public void settleUp(User payer, User payee, BigDecimal amount) {
        balanceSheet.settleUp(payer, payee, amount);
    }

    public BigDecimal getBalance(User userA, User userB) {
        return balanceSheet.getNetBalance(userA, userB);
    }
}
```

*Thread safety — where and why*

`synchronized(ledgerLock)` around `applyExpense`/`settleUp` — a single global lock, not per-user, and that's a deliberate choice: one expense can touch an arbitrary number of participants simultaneously, so a per-user locking scheme would require acquiring multiple locks in a fixed order (sorted by userId) to avoid deadlock — correct but more complex than an interview time-box usually rewards. State this tradeoff explicitly if asked: "I chose a single global lock for atomicity simplicity; per-user lock striping would improve concurrency for large groups but needs consistent lock ordering to avoid deadlock." `ConcurrentHashMap` for the balance maps lets reads (`getNetBalance`) proceed without contending on `ledgerLock`.

*Extensibility*
- New split type → implement `SplitStrategy`, register in the map — `Expense`/`BalanceSheet` untouched
- Groups → wrap participants into a `Group` entity, expenses reference a group instead of a raw list
- **Debt simplification** (strong optional add-on, mention even if out of time): treat net balances as a graph, greedily match the largest creditor with the largest debtor repeatedly (max-heap based) to minimize the total number of settle-up transactions — this is where you can show graph/greedy algorithm chops inside an LLD answer

*Interview walkthrough script*
1. "Split types — equal, exact, percentage — behind one `SplitStrategy` interface, service resolves by key."
2. "Money is `BigDecimal` throughout, never float/double, and equal split explicitly handles the rounding remainder — allocate leftover cents deterministically so no money is silently lost."
3. "Balances live in a nested map, updated atomically per expense under one lock, since a single expense can touch many participants at once."
4. "Optional extension: debt simplification via a greedy graph algorithm to cut the number of real payments needed."

*Connect to your experience*

Same precision discipline as your FX Rate Service — `BigDecimal`, never floating point, for anything money-shaped. The atomic multi-balance update under `applyExpense` is the same shape as your ACID/idempotent transaction processing work — one logical operation touching multiple records must be all-or-nothing, exactly the consistency guarantee your transaction platform enforced.

Your turn — explain it back, or code it cold.