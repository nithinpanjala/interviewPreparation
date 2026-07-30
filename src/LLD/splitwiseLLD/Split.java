package LLD.splitwiseLLD;

import java.math.BigDecimal;

public class Split {
    private final User user;
    private final BigDecimal amountOwed;

    public Split(User user, BigDecimal amountOwed) {
        this.user = user;
        this.amountOwed = amountOwed;
    }

    public User getUser() {
        return user;
    }

    public BigDecimal getAmountOwed() {
        return amountOwed;
    }
}
