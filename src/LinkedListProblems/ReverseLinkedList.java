package LinkedListProblems;

public class ReverseLinkedList {
    public ListNode reverseList(ListNode head) {
        ListNode prev = null;
        ListNode current = head;
        while(current!=null){
            ListNode next = current.next; //save the next
            current.next = prev; // reverse pointing to prev
            prev = current; //moving prev to current
            current = next; //moving current to next
        }
        return prev;
    }
    public ListNode reverseListUsingRecursion(ListNode head) {
        if(head == null || head.next == null){
            return head;
        }
        ListNode newHead = reverseList(head.next); // moves till the last and returns the last node
        head.next.next = head; // make the node's next point to it , reversing the pointer
        head.next = null; // removing the current next as it is pointing forward, now it holds null
        return newHead; // returns the new head pointer
    }
}