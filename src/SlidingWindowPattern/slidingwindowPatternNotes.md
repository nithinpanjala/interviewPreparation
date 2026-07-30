What is the Sliding Window pattern?
A window is a contiguous subarray or substring. You slide it across the input,
expanding or shrinking it based on a condition, instead of recomputing everything
from scratch each step.

The key insight: Instead of checking every possible subarray with nested loops (O(n²)),
 you maintain a window with two pointers — left and right — and move them smartly to
  get O(n).

There are two types you must know:
Fixed window — size is given. Slide rigidly. Easy.
Variable window — size changes based on a condition. This is what Salesforce asks.


Input:  [a, b, c, d, e, f]
         L                    start both pointers at 0
         L  R                 expand right — window valid
         L     R              expand right — window valid
         L        R           expand right — window INVALID
            L     R           shrink left until valid again

The template — burned into memory:
 ``   
    int left = 0;
    int result = 0;
    
    for (int right = 0; right < n; right++) {
        // 1. expand — add s.charAt(right) into your window state
    
        // 2. shrink — while window is invalid, move left forward
        while (/* window is invalid */) {
            // remove s.charAt(left) from window state
            left++;
        }
    
        // 3. update result — window is now valid, record if it's the best
        result = Math.max(result, right - left + 1);
    }

``
Every sliding window problem is just a variation of this template.
The only thing that changes is what "invalid" means and what state you track.


Problem 1 — Longest Substring Without Repeating Characters
This is the most commonly asked sliding window problem at Salesforce. 
Confirmed in multiple MTS/SMTS interviews.
Problem: Given a string, find the length of the longest substring with all unique 
characters



"abcabcbb"  →  3  ("abc")
"bbbbb"     →  1  ("b")
"pwwkew"    →  3  ("wke")

Window invalid condition: a character appears more than once inside the window.
State to track: which characters are currently in the window → HashSet.


``
        public int lengthOfLongestSubstring(String s) {
        Set<Character> window = new HashSet<>();
        int left = 0;
        int best = 0;
        
            for (int right = 0; right < s.length(); right++) {
                char ch = s.charAt(right);
        
                // shrink from left until the duplicate is removed
                while (window.contains(ch)) {
                    window.remove(s.charAt(left));
                    left++;
                }
        
                // window is now valid — ch is unique inside it
                window.add(ch);
                best = Math.max(best, right - left + 1);
            }
        
            return best;
        }
``
Time: O(n) — each character is added and removed at most once.
Space: O(min(n, alphabet)) — the set holds at most the unique chars in the window.
For ASCII that's at most 128.



**Problem 2 — Minimum Window Substring**
This is the harder variant. Also confirmed in Salesforce interviews.
Problem: Given strings s and t, 
find the minimum length substring of s that contains all characters of t.


s = "ADOBECODEBANC",  t = "ABC"  →  "BANC"
s = "a",              t = "a"    →  "a"
s = "a",              t = "aa"   →  ""

What changes from Problem 1:
Instead of a HashSet, you track two frequency maps — one for what you need (need), 
one for what your window currently has (have). 
The window is valid when you've satisfied all character requirements

````
public String minWindow(String s, String t) {
    if (s.isEmpty() || t.isEmpty()) return "";

    // what we need — frequency of each char in t
    Map<Character, Integer> need = new HashMap<>();
    for (char c : t.toCharArray()) {
        need.put(c, need.getOrDefault(c, 0) + 1);
    }

    int left = 0;
    int formed = 0;                  // how many unique chars from t are satisfied
    int required = need.size();      // how many unique chars we need to satisfy

    Map<Character, Integer> window = new HashMap<>();

    int bestLen = Integer.MAX_VALUE;
    int bestLeft = 0, bestRight = 0;

    for (int right = 0; right < s.length(); right++) {
        char ch = s.charAt(right);
        window.put(ch, window.getOrDefault(ch, 0) + 1);

        // check if this char's frequency in window satisfies the requirement
        if (need.containsKey(ch) && window.get(ch).equals(need.get(ch))) {
            formed++;
        }

        // shrink from left while the window is valid (all chars satisfied)
        while (formed == required) {
            // record best
            if (right - left + 1 < bestLen) {
                bestLen = right - left + 1;
                bestLeft = left;
                bestRight = right;
            }

            // remove leftmost char from window
            char leftChar = s.charAt(left);
            window.put(leftChar, window.get(leftChar) - 1);
            if (need.containsKey(leftChar) && window.get(leftChar) < need.get(leftChar)) {
                formed--;            // window no longer satisfies this char
            }
            left++;
        }
    }

    return bestLen == Integer.MAX_VALUE ? "" : s.substring(bestLeft, bestRight + 1);
}


````
Time: O(|s| + |t|)
Space: O(|t|) for the frequency maps

                Problem 1                              Problem 2   
Window        valid whenno duplicates                all chars of t present
State          trackedHashSet                            two HashMaps + formed counter
Shrink when     duplicate found                         window valid (to minimise)
Answer          max window size                         min window substring

In Problem 1 you maximise — so you record the answer after shrinking 
(window is as big as it can be while valid).
In Problem 2 you minimise — so you record the answer while the window is valid,
then shrink to try to make it smaller.


Problem 3 — Max Consecutive Ones After K Flips
Good chance of appearing as a follow-up. This is the same pattern with a twist.
Problem: Given a binary array and integer k, find the max number of consecutive 1s 
if you can flip at most k zeros.


[1,1,1,0,0,0,1,1,1,1,0],  k=2  →  6

Window invalid condition: more than k zeros inside the window.

````

public int longestOnes(int[] nums, int k) {
    int left = 0;
    int zeros = 0;
    int best = 0;

    for (int right = 0; right < nums.length; right++) {
        if (nums[right] == 0) zeros++;

        // too many zeros — shrink until we're back to k zeros
        while (zeros > k) {
            if (nums[left] == 0) zeros--;
            left++;
        }

        best = Math.max(best, right - left + 1);
    }

    return best;
}

````
Notice: the template is almost identical to Problem 1. 
The only thing that changed is the 
invalid condition — from window.contains(ch) to zeros > k. 
This is the power of having the pattern locked in.

How to explain Sliding Window in the interview
When you see the problem:
The moment you see "subarray", "substring", "contiguous", 
"longest/shortest that satisfies some condition" — that's your trigger to 
think sliding window.


What to say:

"This looks like a sliding window problem. I'll use two pointers, 
left and right, to maintain a window of valid characters.
Right expands to include new elements, and left shrinks whenever the window violates 
the constraint. This gives me O(n) instead of the O(n²) brute force."

Complexity to state:
"Each element is visited at most twice — once when right passes it, 
once when left passes it. 
So it's O(n) time, O(k) space where k is the size of the character set or window state."


Your practice instructions
Do these three in order, from scratch in your IDE:

lengthOfLongestSubstring — should take you under 10 minutes now
longestOnes — 5 minutes, it's nearly the same template
minWindow — the hardest of the three, give it 20 minutes

The pattern to engrain: right always moves forward, 
left only moves forward, window state is updated on both ends.
You will never move left or right backwards.

Come back once you've attempted at least Problem 1 and 2 — then we move to Merge Intervals, which is Day 1 Question 3.



Pattern Recognition

When you see:

longest valid substring
continuous segment
distinct constraints
at most k conditions
window expansion/shrinking