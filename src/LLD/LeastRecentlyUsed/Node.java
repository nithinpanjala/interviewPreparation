package LLD.LeastRecentlyUsed;

public class Node {
    String key;
    String value;
    Node next;
    Node prev;
    public Node(String key, String value){
        this.key = key;
        this.value = value;
    }
}