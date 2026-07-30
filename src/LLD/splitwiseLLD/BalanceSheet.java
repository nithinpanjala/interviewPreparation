package LLD.splitwiseLLD;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
