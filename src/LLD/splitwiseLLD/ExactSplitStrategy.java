package LLD.splitwiseLLD;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
