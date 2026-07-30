package SlidingWindowPattern;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MinimumWindowSubstring {
    public String MinimumWindowSubstringMethod(String s, String t){
        if(s.isEmpty() || t.isEmpty()) return "";
        Map<Character, Integer> needMap = new HashMap<>();
        for(char c: t.toCharArray()){
            needMap.put(c,needMap.getOrDefault(c,0)+1);
        }
        int formed = 0;
        int required = needMap.size();
        int left =0;
        int bestLength = Integer.MAX_VALUE;
        int bestLeft = 0;
        int bestRight = 0;
        Map<Character,Integer> window = new HashMap<>();
        for(int right = 0; right< s.length(); right++){
            char c = s.charAt(right);
            window.put(c,window.getOrDefault(c,0)+1);

            if(needMap.containsKey(c) && Objects.equals(needMap.get(c), window.get(c)))formed++;
            while(formed == required){
                if(right-left+1 < bestLength){
                    bestLength = right-left+1;
                    bestRight = right;
                    bestLeft =left;
                }
                char leftChar = s.charAt(left);
                window.put(leftChar, window.get(leftChar)-1);

                if(needMap.containsKey(leftChar) && window.get(leftChar)<needMap.get(leftChar))formed--;
                left++;
            }
        }
        return bestLength == Integer.MAX_VALUE ? "": s.substring(bestLeft,bestRight+1);
    }
}
