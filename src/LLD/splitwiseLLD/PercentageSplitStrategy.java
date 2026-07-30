package LLD.splitwiseLLD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
