package LLD.splitwiseLLD;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SplitStrategy {
    // inputValues meaning depends on strategy: null for equal, exact amounts for Exact, percentages for Percentage
    List<Split> calculateSplits(BigDecimal totalAmount, List<User> participants, Map<User, BigDecimal> inputValues);
}
