package LLD.LeastRecentlyUsed;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class ThreadSafeLRUCache {

    private static class Node {
        int key, val;
        Node prev, next;
        Node(int key, int val) { this.key = key; this.val = val; }
    }

    private final int capacity;
    private final Map<Integer, Node> map;
    private final Node head, tail;

    // ReadWriteLock: many threads can read simultaneously,
    // but writes are exclusive — better throughput than synchronized
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock  readLock  = lock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = lock.writeLock();

    public ThreadSafeLRUCache(int capacity) {
        this.capacity = capacity;
        this.map  = new HashMap<>();
        this.head = new Node(0, 0);
        this.tail = new Node(0, 0);
        head.next = tail;
        tail.prev = head;
    }

    public int get(int key) {
        // get() MOVES the node → it mutates structure → needs write lock
        writeLock.lock();
        try {
            if (!map.containsKey(key)) return -1;
            Node node = map.get(key);
            moveToFront(node);
            return node.val;
        } finally {
            writeLock.unlock(); // always release in finally
        }
    }

    public void put(int key, int value) {
        writeLock.lock();
        try {
            if (map.containsKey(key)) {
                Node node = map.get(key);
                node.val = value;
                moveToFront(node);
            } else {
                if (map.size() == capacity) evictLRU();
                Node node = new Node(key, value);
                map.put(key, node);
                insertAtFront(node);
            }
        } finally {
            writeLock.unlock();
        }
    }

    // ── same helpers as before ────────────────────────────────────────────
    private void moveToFront(Node node) { detach(node); insertAtFront(node); }
    private void detach(Node node) { node.prev.next = node.next; node.next.prev = node.prev; }
    private void insertAtFront(Node node) {
        node.next = head.next; node.prev = head;
        head.next.prev = node; head.next = node;
    }
    private void evictLRU() { Node lru = tail.prev; detach(lru); map.remove(lru.key); }
}