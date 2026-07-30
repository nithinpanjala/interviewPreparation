package CycleDetection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
https://leetcode.com/problems/course-schedule/description/

There are a total of numCourses courses you have to take, labeled from 0 to numCourses - 1. You are given an array prerequisites where prerequisites[i] = [ai, bi] indicates that you must take course bi first if you want to take course ai.

For example, the pair [0, 1], indicates that to take course 0 you have to first take course 1.
Return true if you can finish all courses. Otherwise, return false.
Example 1:
Input: numCourses = 2, prerequisites = [[1,0]]
Output: true
Explanation: There are a total of 2 courses to take.
To take course 1 you should have finished course 0. So it is possible.
Example 2:
Input: numCourses = 2, prerequisites = [[1,0],[0,1]]
Output: false
Explanation: There are a total of 2 courses to take.
To take course 1 you should have finished course 0, and to take course 0 you should also have finished course 1. So it is impossible.

Constraints:
1 <= numCourses <= 2000
0 <= prerequisites.length <= 5000
prerequisites[i].length == 2
0 <= ai, bi < numCourses
All the pairs prerequisites[i] are unique.

Follow-up: Topological Order (LeetCode 210)
If there's no cycle, what ORDER should you take courses?
 */
public class CycleInDirectedGraph {
    public int[] canFinish(int numCourses, int[][] prerequisites) {
        List<Integer> order = new ArrayList<>();

        List<List<Integer>> graph = new ArrayList<>();
        for(int i=0;i<numCourses;i++){
            graph.add(new ArrayList<>());
        }
        for(int[] pre: prerequisites){
            graph.get(pre[1]).add(pre[0]);
        }
        int[] states = new int[numCourses];
        for(int i =0;i<numCourses;i++){
            if(states[i] == 0){
                if(hasCycle(i,graph,states, order)){
                    return (new int[0]);
                }
            }
        }
        Collections.reverse(order);
        return order.stream().mapToInt(Integer::intValue).toArray();
       // return true;
    }

    public boolean hasCycle(int node, List<List<Integer>> graph, int[] states, List<Integer> order){
        states[node] = 1;
        for(int neighbour: graph.get(node)){
            if(states[neighbour] == 0){
                if(hasCycle(neighbour,graph,states, order)){
                    return true;
                }
            }
            if(states[neighbour] == 1){
                return true;
            }
        }
        states[node] =2;
        order.add(node);  // Add when fully done → topological order (reversed)
        return false;
    }
}
