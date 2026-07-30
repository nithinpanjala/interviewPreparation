package MergeIntervalsProblems;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MinimumRoomsNeeded {
    public int minMeetingRooms(int[][] intervals) {
        int rooms = 0;
        Arrays.sort(intervals,(a,b)->Integer.compare(a[0],b[0]));
        int[] starts = new int[intervals.length];
        int[] ends = new int[intervals.length];
        for(int i =0;i< intervals.length;i++){
            starts[i]= intervals[i][0];
            ends[i] = intervals[i][1];
        }
        int j =0;
        for(int i=0;i< intervals.length;i++){
            if(starts[i]<ends[j]){
                rooms++;
            }else{
                j++;
            }
        }
        return rooms;
    }
}