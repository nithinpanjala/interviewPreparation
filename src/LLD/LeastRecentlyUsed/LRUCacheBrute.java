package LLD.LeastRecentlyUsed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Brute force — DO NOT use in interview, only explain it to show thinking
class LRUCacheBrute {
    private final int capacity;
    private final Map<Integer, Integer> map;
    private final List<Integer> order; // tracks recency

    public LRUCacheBrute(int capacity) {
        this.capacity = capacity;
        this.map = new HashMap<>();
        this.order = new ArrayList<>();
    }

    public int get(int key) {
        if (!map.containsKey(key)) return -1;
        order.remove(Integer.valueOf(key)); // O(n) — the bottleneck
        order.add(key);
        return map.get(key);
    }

    public void put(int key, int value) {
        if (map.containsKey(key)) {
            order.remove(Integer.valueOf(key));
        } else if (map.size() == capacity) {
            int lru = order.remove(0); // O(n)
            map.remove(lru);
        }
        order.add(key);
        map.put(key, value);
    }
}