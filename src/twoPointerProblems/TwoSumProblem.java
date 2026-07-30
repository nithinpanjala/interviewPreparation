package twoPointerProblems;

import java.util.HashMap;
import java.util.Map;

public class TwoSumProblem {

//    Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to target.
//
//    You may assume that each input would have exactly one solution, and you may not use the same element twice.
//
//    You can return the answer in any order.
    public int[] twoSum(int[] nums, int target) {
        Map<Integer,Integer> twoSumMap = new HashMap<>();
        for(int i= 0;i<nums.length;i++){
            int compliment = target-nums[i];
            if(twoSumMap.containsKey(compliment)){
                return new int[]{twoSumMap.get(compliment), i};
            }
            twoSumMap.put(nums[i],i);
        }
        return new int[] {};
    }
}