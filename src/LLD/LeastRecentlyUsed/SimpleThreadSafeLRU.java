package LLD.LeastRecentlyUsed;

// Simplest thread-safe version — coarse-grained lock on every method
class SimpleThreadSafeLRU {
    private final LRUCache cache;

    SimpleThreadSafeLRU(int capacity) { cache = new LRUCache(capacity); }

    public synchronized String get(String key)           { return cache.get(key); }
    public synchronized void put(String key, String val) { cache.put(key, val); }
}