package LinkedListProblems;

public class ReverseKGroups {

    public ListNode reverseKGroup(ListNode head, int k) {

        ListNode dummy = new ListNode(0);
        dummy.next = head;

        // lastReversedTail: the TAIL of the portion already reversed
        // starts at dummy, advances after each group
        ListNode lastReversedTail = dummy;

        while (true) {

            // ── STEP 1: Can we form a full group? ──────────────────────
            // Walk k steps. If we fall off the end → stop.
            ListNode kthNode = getKthNode(lastReversedTail, k);
            if (kthNode == null) break;

            // ── STEP 2: Mark boundaries ────────────────────────────────
            ListNode groupHead    = lastReversedTail.next; // first node of this group
            ListNode nextGroupHead = kthNode.next;          // first node of NEXT group

            // ── STEP 3: Reverse nodes inside the group ─────────────────
            // We reverse from groupHead up to (but not including) nextGroupHead
            //
            // tail points to nextGroupHead at start — so when groupHead.next
            // gets set to tail in the first iteration, the group tail
            // automatically connects to the next group. No extra step needed.

            ListNode tail    = nextGroupHead; // where the group tail should point after reversal
            ListNode current = groupHead;     // node we're currently reversing

            while (current != nextGroupHead) {
                ListNode saveNext = current.next; // save before we overwrite
                current.next = tail;              // reverse: point current backward
                tail    = current;                // tail advances to current
                current = saveNext;               // current moves forward
            }
            // After loop: tail = kthNode (new group head after reversal)

            // ── STEP 4: Stitch reversed group into the main list ───────
            //
            // Before stitch:
            //   lastReversedTail → groupHead → ... → kthNode → nextGroupHead
            //
            // After reversal inside group:
            //   kthNode → ... → groupHead → nextGroupHead
            //
            // We need:
            //   lastReversedTail → kthNode → ... → groupHead → nextGroupHead

            lastReversedTail.next = kthNode;   // connect previous tail to new group head
            lastReversedTail      = groupHead; // groupHead is now the new group tail

            // lastReversedTail.next (= groupHead.next) already points to nextGroupHead
            // because we set it during reversal (tail started as nextGroupHead)
        }

        return dummy.next;
    }

    // Walk exactly k steps from startNode
// Returns the node we land on, or null if not enough nodes
    private ListNode getKthNode(ListNode startNode, int k) {
        ListNode node = startNode;
        while (node != null && k > 0) {
            node = node.next;
            k--;
        }
        return node;
    }
}