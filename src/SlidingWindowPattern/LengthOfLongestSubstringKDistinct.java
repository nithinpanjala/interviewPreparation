package SlidingWindowPattern;

import java.util.HashMap;
import java.util.Map;

public class LengthOfLongestSubstringKDistinct {

    public int lengthOfLongestSubstringKDistinctCharacters(String s, int k){
        int left =0;
        Map<Character, Integer> map = new HashMap<>();
        int bestLength = 0;
        for(int right = 0 ; right< s.length();right ++){
            char c = s.charAt(right);
            System.out.println("Iterating character :"+c);
            map.put(c,map.getOrDefault(c,0)+1);
            System.out.println("current Map :"+map.toString());
            while(map.size()>k){
                if(map.get(s.charAt(left))== 1 ){
                    map.remove(s.charAt(left));
                }else{
                    map.put(s.charAt(left),map.get(s.charAt(left))-1);
                }
                left++;
            }
            bestLength = Math.max(bestLength,right-left+1);
            System.out.println("current bestLength :"+bestLength);
        }
        return bestLength;
    }
}
