package SlidingWindowPattern;

import java.util.Arrays;

public class LongestOnes {

    public int longestOnesWithKFlips(int[] arr, int k){
       int result = 0;
       int left = 0;
       int countOfZeros = 0;
       int leftIndex = 0;
       int rightIndex = 0;
       for (int right =0;right<arr.length; right++){
           if(arr[right]==0) countOfZeros++;
           while(countOfZeros>k){
               if(arr[left] ==0) countOfZeros--;
               left++;
           }
           if(result< right-left+1){
               leftIndex = left;
               rightIndex = right;
           }
           result = Math.max(result, right-left+1);
       }
       int[] resultArr = Arrays.copyOfRange(arr,leftIndex,rightIndex+1);
       System.out.println("longestOnesWithKZeroFlips subArray is :"+ Arrays.toString(resultArr));
       return result;
    }
}
