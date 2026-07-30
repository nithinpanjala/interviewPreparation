package LLD.LeastRecentlyUsed;

import java.util.HashMap;
import java.util.Map;

public class LRUCache{
    int capacity;
    Map<String,Node> map;
    Node head;
    Node tail;

    public LRUCache(int capacity){
        this.capacity = capacity;
        map = new HashMap<>();
        head = new Node("head","head");
        tail = new Node("tail","tail");
        head.next = tail;
        tail.prev = head;
    }

    public String get(String key){
        if(!map.containsKey(key)){
            return "no key found";
        }else{
            Node getNode = map.get(key);
            moveToFront(getNode);
            return getNode.value;
        }
    }

    public void put(String key, String value){
        if(map.size()== capacity){
            evictLRU();
        }
        if(map.containsKey(key)){
            Node alreadyAvailableNode = map.get(key);
            alreadyAvailableNode.value = value;
            map.put(key,alreadyAvailableNode);
            moveToFront(alreadyAvailableNode);
        }else{
            Node putNode = new Node(key,value);
            map.put(key,putNode);
            moveToFront(putNode);
        }
    }

    private void evictLRU(){
        Node evictNode = tail.prev;
        detachNode(evictNode);
        map.remove(evictNode.value);
    }

    private void moveToFront(Node node){
        detachNode(node);
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    private void detachNode(Node node){
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }


}