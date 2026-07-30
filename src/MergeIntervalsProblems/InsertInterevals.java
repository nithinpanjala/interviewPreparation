package MergeIntervalsProblems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InsertInterevals {
    public int[][] insert(int[][] intervals, int[] newInterval) {
        System.out.println("insert: "+ Arrays.deepToString(intervals));
        System.out.println("newInterval: "+ Arrays.toString(newInterval));
        if(intervals.length==0) return new int[][]{newInterval};
        if(newInterval.length==0) return intervals;
        List<int[]> result = new ArrayList<>();

        int[] current = intervals[0];
        if(newInterval[0]<=current[0] && newInterval[0]<=current[1]){
            current = newInterval;
        } else if(newInterval[0]<=current[0]){
            current[0] = newInterval[0];
            current[1] = Math.max(current[1],newInterval[1]);
        }
        result.add(current);
        System.out.println("initialization: "+ Arrays.toString(current));

        for(int i=0;i< intervals.length;i++){
            int[] next = intervals[i];
            System.out.println("next: "+ Arrays.toString(next));
            System.out.println("current: "+ Arrays.toString(current));

           if(current[1]>=newInterval[0]){
               current[1] = Math.max(current[1],newInterval[1]);
           }else if (current[1]<=newInterval[0] && newInterval[0]<=next[0] && newInterval[1]>next[0]){
               next[0] = newInterval[0];
               next[1] = Math.max(next[1],newInterval[1]);
               System.out.println("next check: "+ Arrays.toString(next));

           }else if(newInterval[0]<=next[0] && newInterval[1]<next[0]){
               result.add(newInterval);
               current = newInterval;
           }else if(next[1]>=newInterval[0]){
               next[1] = Math.max(next[1],newInterval[1]);
           }
            if(current[1]>=next[0]){
                current[1] = Math.max(current[1],next[1]);
            } else{
                result.add(next);
                current = next;
            }
            if(i==intervals.length-1 && current[1]<newInterval[0]){
                result.add(newInterval);
            }
        }
        return result.toArray(new int[result.size()][]);
    }
    public int[][] insertOptima(int[][] intervals, int[] newInterval){
        int i = 0;
        int n = intervals.length;
        List<int[]> result = new ArrayList<>();
        while(intervals[i][1]<newInterval[0]){
            result.add(intervals[i]);
            i++;
        }

        while(i<n && intervals[i][0]<=newInterval[1]){
            newInterval[0] = Math.min(intervals[i][0],newInterval[0]);
            newInterval[1] = Math.max(intervals[i][1],newInterval[1]);
            i++;
        }
        result.add(newInterval);
        while(i<n){
            result.add(intervals[i]);
            i++;
        }
        return result.toArray(new int[result.size()][]);
    }
}
