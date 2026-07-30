package MergeIntervalsProblems;

import java.util.Arrays;

public class NonOverlappingIntervals {
    public int eraseOverlapIntervals(int[][] intervals) {
        int count = 0;
        if(intervals.length ==0) return 0;
        if(intervals.length ==1) return 1;
        Arrays.sort(intervals, (a,b)->Integer.compare(a[1],b[1]));
//        Arrays.sort(intervals, (a, b) -> {
//            if (a[0] == b[0]) {
//                return Integer.compare(a[1],b[1]);
//            }
//            return Integer.compare(a[0],b[0]) ;
//        });
        System.out.println("intervals sorted continues: "+ Arrays.deepToString(intervals));
        int[] current = intervals[0];
        int previousEnd = intervals[0][1];

        for(int i =1; i< intervals.length; i++){
            int[] next = intervals[i];
            System.out.println("current "+ Arrays.toString(current));
            System.out.println("next "+ Arrays.toString(next));
            int currentStart = intervals[i][0];

            if(previousEnd>currentStart) {
                System.out.println("current[1] "+ current[1]);
                System.out.println("next[0] "+ next[0]);

                count++;
            }else{
                current = next;
                previousEnd = intervals[i][1];
            }

        }

        return count;

    }
}
