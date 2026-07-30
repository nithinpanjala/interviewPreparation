package LLD.splitwiseLLD;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
