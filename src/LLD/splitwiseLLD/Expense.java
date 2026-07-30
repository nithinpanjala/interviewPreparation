package LLD.splitwiseLLD;


import java.math.BigDecimal;
import java.util.List;

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


