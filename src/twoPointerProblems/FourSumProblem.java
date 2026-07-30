package twoPointerProblems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FourSumProblem {

//    Given an array nums of n integers, return an array of all the unique quadruplets [nums[a], nums[b], nums[c], nums[d]] such that:
//
//            0 <= a, b, c, d < n
//    a, b, c, and d are distinct.
//    nums[a] + nums[b] + nums[c] + nums[d] == target
//    You may return the answer in any order.
//

    public List<List<Integer>> fourSum(int[] nums, int target) {

        List<List<Integer>> result = new ArrayList<>();

        Arrays.sort(nums);

        for (int i = 0; i < nums.length - 3; i++) {

            // Skip duplicate i
            if (i > 0 && nums[i] == nums[i - 1]) {
                continue;
            }

            int j = i + 1;

            while (j < nums.length - 2) {

                // Skip duplicate j
                if (j > i + 1 && nums[j] == nums[j - 1]) {
                    j++;
                    continue;
                }

                int left = j + 1;
                int right = nums.length - 1;

                while (left < right) {

                    long sum = (long) nums[i]
                            + nums[j]
                            + nums[left]
                            + nums[right];

                    if (sum == target) {

                        result.add(Arrays.asList(
                                nums[i],
                                nums[j],
                                nums[left],
                                nums[right]
                        ));

                        left++;
                        right--;

                        // Skip duplicate left
                        while (left < right &&
                                nums[left] == nums[left - 1]) {
                            left++;
                        }

                        // Skip duplicate right
                        while (left < right &&
                                nums[right] == nums[right + 1]) {
                            right--;
                        }

                    } else if (sum < target) {

                        left++;

                    } else {

                        right--;
                    }
                }

                j++;
            }
        }

        return result;
    }
}