package SlidingWindowPattern;

//Pattern Recognition
//
//When you see:
//
//longest valid substring
//continuous segment
//distinct constraints
//at most k conditions
//window expansion/shrinking


public class subarraySumProblem {
//     Given an array of integers nums and an integer k, return the total number of subarrays whose sum equals to k.

// A subarray is a contiguous non-empty sequence of elements within an array.

    public int subarraySum(int[] nums, int k) {
        int result =0;
        for(int i=0;i<nums.length-1;i++){
            int j=i+1;
            int sum = nums[i]+nums[j];
            if(sum==k){
                result++;
            }
            while(j<nums.length-1 && sum<=k){
                j++;
                sum = sum +nums[j];
                if(sum==k){
                    result++;
                }

            }
        }
        if(nums[nums.length-1]==k){
            result++;
        }
        return result;
    }
}