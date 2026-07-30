package MergeIntervalsProblems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeIntervals {
        public int[][] merge(int[][] intervals) {
            System.out.println("merge: "+ Arrays.deepToString(intervals));
            Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
            System.out.println("sorted: "+ Arrays.deepToString(intervals));
            List<int[]> result = new ArrayList<>();

            int[] current = intervals[0];
            result.add(current);
            for(int i = 1;i<intervals.length;i++){
                int[] next = intervals[i];
                System.out.println("current: "+ Arrays.toString(current));
                System.out.println("next: "+ Arrays.toString(next));

                if(current[1]>=next[0]){
                    current[0] = Math.min(current[0],next[0]);
                    current[1] = Math.max(current[1],next[1]);
                    System.out.println("current after merge: "+ Arrays.toString(current));
                }else{
                    result.add(next);
                    System.out.println("result after adding: "+ result);
                    current = next;
                }
            }
            return result.toArray(new int[result.size()][]);
        }
}
