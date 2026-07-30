import LinkedListProblems.ListNode;
import LinkedListProblems.ReverseKGroups;
import MergeIntervalsProblems.*;
import SlidingWindowPattern.LengthOfLongestSubArrayProblem;
import SlidingWindowPattern.LengthOfLongestSubstringKDistinct;
import SlidingWindowPattern.LongestOnes;
import SlidingWindowPattern.MinimumWindowSubstring;

import java.sql.Array;
import java.util.Arrays;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

//       LengthOfLongestSubArrayProblem lengthOfLongestSubArrayProblem = new LengthOfLongestSubArrayProblem();
//        int k = lengthOfLongestSubArrayProblem.lengthOfLongestSubstringOptimalAnInt("pwwkew");
//        System.out.println("result : "+k);


//        LongestOnes longestOnes = new LongestOnes();
//        int[] arr = {1,1,1,0,0,0,1,1,1,1,0};
//        int longestOnesWithKZeroFlips = longestOnes.longestOnesWithKFlips(arr,2);
//        System.out.println("longestOnesWithKZeroFlips : "+longestOnesWithKZeroFlips);
//

//
//        MinimumWindowSubstring minimumWindowSubstring = new MinimumWindowSubstring();
//        String minimumWindowSubstringMethod = minimumWindowSubstring.MinimumWindowSubstringMethod("ADOBECODEBANC","ABC");
//        System.out.println("minimumWindowSubstringMethod : "+minimumWindowSubstringMethod);


//        LengthOfLongestSubstringKDistinct lengthOfLongestSubstringKDistinct = new LengthOfLongestSubstringKDistinct();
//        int lengthOfLongestSubstringKDistinctCharacters =lengthOfLongestSubstringKDistinct.lengthOfLongestSubstringKDistinctCharacters("aabbccddeeeffffghhhhhhhhhhhhh",2);
//        System.out.println("lengthOfLongestSubstringKDistinctCharacters : "+lengthOfLongestSubstringKDistinctCharacters);
//
//        MergeIntervals mergeIntervals = new MergeIntervals();
//        int[][] arr = {{1, 2},{11,17},{19,25}, {3, 5},{4,8},{6,9}};
//        System.out.println("MergeIntervals: "+ Arrays.deepToString(mergeIntervals.merge(arr)));


//        int[][] arr = {{3, 5},{12,15}};
//        int[] arr2 = {6,6};
//        InsertInterevals insertInterevals = new InsertInterevals();
//        System.out.println("MergeIntervals: "+ Arrays.deepToString(insertInterevals.insert(arr,arr2)));


//        MeetingRooms meetingRooms =new MeetingRooms();
//        int[][] arr = {{3, 5},{1,2}};
//        int[][] arr1 = {{3, 5},{1,4}};
//        int[][] arr2 = {{3, 5},{5,6}};
//        System.out.println("meetingRooms no overlap : "+ meetingRooms.canAttendMeetings(arr));
//        System.out.println("meetingRooms overlap : "+ meetingRooms.canAttendMeetings(arr1));
//        System.out.println("meetingRooms continues: "+ meetingRooms.canAttendMeetings(arr2));
//
//

//        MinimumRoomsNeeded minimumRoomsNeeded = new MinimumRoomsNeeded();
//        int[][] arr = {{3, 5},{1,2}};
//        int[][] arr1 = {{3, 5},{1,4}};
//        int[][] arr2 = {{3, 5},{5,6}};
//        System.out.println("meetingRooms no overlap : "+ minimumRoomsNeeded.minMeetingRooms(arr));
//        System.out.println("meetingRooms overlap : "+ minimumRoomsNeeded.minMeetingRooms(arr1));
//        System.out.println("meetingRooms continues: "+ minimumRoomsNeeded.minMeetingRooms(arr2));


//        NonOverlappingIntervals nonOverlappingIntervals = new NonOverlappingIntervals();
//        int[][] arr2 = {{1, 100},{11,22},{1,11},{2,12}};
//        System.out.println("nonOverlappingIntervals continues: "+ nonOverlappingIntervals.eraseOverlapIntervals(arr2));


        // Create input: head = [1,2,3,4,5]
        ListNode head = new ListNode(1);
        head.next = new ListNode(2);
        head.next.next = new ListNode(3);
        head.next.next.next = new ListNode(4);
        head.next.next.next.next = new ListNode(5);

        int k = 2;

        ReverseKGroups solution = new ReverseKGroups();

        ListNode result = solution.reverseKGroup(head, k);

        printList(result);
    }
    private static void printList(ListNode head) {

        while(head != null) {
            System.out.print(head.val + " ");
            head = head.next;
        }
    }
}
