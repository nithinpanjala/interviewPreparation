package SlidingWindowPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
Given a string s, find the length of the longest substring without
 duplicate characters.



Example 1:

Input: s = "abcabcbb"
Output: 3
Explanation: The answer is "abc", with the length of 3.
Note that "bca" and "cab" are also correct answers.
Example 2:

Input: s = "bbbbb"
Output: 1
Explanation: The answer is "b", with the length of 1.
 */

public class LengthOfLongestSubArrayProblem {
    public int lengthOfLongestSubstring(String s) {
        Set<Character> set = new HashSet<>();
        int left = 0;
        int result = 0;
        char[] sArray = s.toCharArray();
        for(int right = 0; right<s.length(); right++){
            System.out.println("right : "+right);
            while(set.contains(sArray[right])){
                System.out.println("set contain the right removing the left : "+right);
                set.remove(sArray[left]);
                left++;
            }
            set.add(sArray[right]);
            result = Math.max(result, right-left+1);
            System.out.println("result in iteration: "+result);
        }
        return result;
    }

        public int lengthOfLongestSubstringOptimalAnInt(String s) {
            Map<Character, Integer> map = new HashMap<>();
            int left = 0;
            int maxLength = 0;
            for (int right = 0; right < s.length(); right++) {
                char ch = s.charAt(right);
                if (map.containsKey(ch)) {
                    left = Math.max(left, map.get(ch) + 1);
                }
                map.put(ch, right);
                maxLength = Math.max(maxLength, right - left + 1);
            }
            return maxLength;
        }

}
