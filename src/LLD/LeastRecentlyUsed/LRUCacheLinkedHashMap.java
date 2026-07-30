package LLD.LeastRecentlyUsed;

import java.util.LinkedHashMap;
import java.util.Map;

class LRUCacheLinkedHashMap extends LinkedHashMap<Integer, Integer> {

    private final int capacity;

    public LRUCacheLinkedHashMap(int capacity) {
        // accessOrder = true → orders by access, not insertion
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    public int get(int key) {
        return super.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        super.put(key, value);
    }

    // Called automatically by LinkedHashMap on every put()
    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > capacity;
    }
}