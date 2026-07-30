package LLD.splitwiseLLD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
