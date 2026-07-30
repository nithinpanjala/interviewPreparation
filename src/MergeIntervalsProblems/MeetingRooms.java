package MergeIntervalsProblems;

import java.util.Arrays;

public class MeetingRooms {
    public boolean canAttendMeetings(int[][] intervals) {
        Arrays.sort(intervals, (a,b)-> Integer.compare( a[0], b[0]));
        if(intervals.length==1) return true;
        int[] current = intervals[0];
        for(int i=1;i<intervals.length;i++){
            int[] next = intervals[i];
            if(current[1]>next[0]) return false;
        }
        return true;
    }
}
