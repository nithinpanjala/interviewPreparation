# 🎯 ULTIMATE DSA INTERVIEW CHEAT SHEET
## Complete Data Structures Reference • Java Collections • Methods • Usage Patterns • Time Complexity
**Your Go-To Reference for Every DSA Interview**

---

## TABLE OF CONTENTS

1. [ARRAYS & LISTS](#arrays-lists)
2. [LINKED LISTS](#linked-lists)
3. [STACKS](#stacks)
4. [QUEUES](#queues)
5. [HASH STRUCTURES (Map, Set)](#hash-structures)
6. [TREES](#trees)
7. [GRAPHS](#graphs)
8. [HEAP / PRIORITY QUEUE](#heap-priority-queue)
9. [TRIE](#trie)
10. [UNION-FIND (Disjoint Set)](#union-find)
11. [JAVA COLLECTIONS FRAMEWORK](#java-collections)
12. [STREAMS API](#streams-api)
13. [COMPARABLE & COMPARATOR](#comparable-comparator)
14. [COMMON PATTERNS & TECHNIQUES](#common-patterns)
15. [TIME COMPLEXITY CHEAT SHEET](#time-complexity)
16. [WHEN TO USE WHAT](#when-to-use-what)

---

<a name="arrays-lists"></a>
# 1. ARRAYS & LISTS

## Array

```java
int[] arr = new int[10];                    // Fixed size
int[] arr = {1, 2, 3, 4, 5};               // Initialize with values
arr[0] = 10;                                // Access: O(1)
int length = arr.length;                    // Get length

// Multi-dimensional
int[][] matrix = new int[3][4];
int[][] matrix = {{1,2,3}, {4,5,6}};
```

### Key Methods:
```java
Arrays.toString(arr);                       // Convert to string
Arrays.sort(arr);                           // Sort: O(n log n)
Arrays.fill(arr, value);                    // Fill with value: O(n)
Arrays.copyOf(arr, newLength);              // Copy array
Arrays.binarySearch(arr, key);              // Binary search: O(log n)
Arrays.equals(arr1, arr2);                  // Compare arrays
System.arraycopy(src, srcPos, dest, ...);  // Copy portion: O(n)
```

### Time Complexity:
| Operation | Time |
|-----------|------|
| Access | O(1) |
| Search (unsorted) | O(n) |
| Search (sorted) | O(log n) |
| Insert | O(n) |
| Delete | O(n) |
| Sort | O(n log n) |

---

## ArrayList (Dynamic Array)

```java
List<Integer> list = new ArrayList<>();
List<Integer> list = new ArrayList<>(10);      // Initial capacity
List<Integer> list = Arrays.asList(1, 2, 3);  // Create from array
```

### Key Methods:
```java
// Add/Remove
list.add(5);                    // O(1) amortized
list.add(0, 10);               // O(n) - insert at index
list.remove(0);                // O(n)
list.remove(Integer.valueOf(5)); // O(n)
list.clear();                  // O(n)

// Access
list.get(0);                   // O(1)
list.set(0, 10);              // O(1)
list.indexOf(5);              // O(n)
list.lastIndexOf(5);          // O(n)
list.contains(5);             // O(n)

// Size & Capacity
list.size();                  // O(1)
list.isEmpty();               // O(1)
list.ensureCapacity(20);      // Allocate capacity

// Iteration
for (int x : list) { }
for (int i = 0; i < list.size(); i++) { }
list.forEach(x -> System.out.println(x));

// Sublist
list.subList(0, 2);           // Views elements from 0 to 1

// Sorting
Collections.sort(list);       // O(n log n)
Collections.reverse(list);    // O(n)
Collections.shuffle(list);    // O(n)
```

### Time Complexity:
| Operation | Time |
|-----------|------|
| Access | O(1) |
| Append | O(1) amortized |
| Insert at index | O(n) |
| Remove at index | O(n) |
| Remove by value | O(n) |
| Search | O(n) |

---

## LinkedList (Doubly-Linked List)

```java
List<Integer> list = new LinkedList<>();
Deque<Integer> deque = new LinkedList<>();
```

### Key Methods:
```java
// Add
list.add(5);                // O(1) - append
list.add(0, 10);            // O(n) - insert
list.addFirst(1);           // O(1)
list.addLast(5);            // O(1)

// Remove
list.remove();              // O(1) - remove first
list.removeFirst();         // O(1)
list.removeLast();          // O(1)
list.remove(0);             // O(n) - remove at index
list.remove(Integer.valueOf(5)); // O(n)

// Access
list.get(0);               // O(n) - worse than ArrayList
list.getFirst();           // O(1)
list.getLast();            // O(1)
list.peek();               // O(1) - get first without removing
list.peekFirst();          // O(1)
list.peekLast();           // O(1)

// Check
list.contains(5);          // O(n)

// Size
list.size();               // O(1)

// Use as Deque (Double-ended queue)
Deque<Integer> dq = new LinkedList<>();
dq.addFirst(1);  dq.addLast(5);
dq.removeFirst(); dq.removeLast();
dq.peekFirst();  dq.peekLast();
```

### Time Complexity:
| Operation | Time |
|-----------|------|
| Add first | O(1) |
| Add last | O(1) |
| Add at index | O(n) |
| Remove first | O(1) |
| Remove last | O(1) |
| Remove at index | O(n) |
| Access at index | O(n) |

---

<a name="linked-lists"></a>
# 2. LINKED LISTS (Manual Implementation)

## Singly Linked List

```java
class Node {
    int val;
    Node next;
    
    Node(int val) {
        this.val = val;
    }
}

class SinglyLinkedList {
    Node head;
    
    // Insert at beginning: O(1)
    void insertFirst(int val) {
        Node newNode = new Node(val);
        newNode.next = head;
        head = newNode;
    }
    
    // Insert at end: O(n)
    void insertLast(int val) {
        Node newNode = new Node(val);
        if (head == null) {
            head = newNode;
            return;
        }
        Node curr = head;
        while (curr.next != null) {
            curr = curr.next;
        }
        curr.next = newNode;
    }
    
    // Delete: O(n)
    void delete(int val) {
        if (head == null) return;
        if (head.val == val) {
            head = head.next;
            return;
        }
        Node prev = head;
        Node curr = head.next;
        while (curr != null) {
            if (curr.val == val) {
                prev.next = curr.next;
                return;
            }
            prev = curr;
            curr = curr.next;
        }
    }
    
    // Traverse: O(n)
    void display() {
        Node curr = head;
        while (curr != null) {
            System.out.print(curr.val + " -> ");
            curr = curr.next;
        }
        System.out.println("null");
    }
}
```

---

## Doubly Linked List

```java
class DNode {
    int val;
    DNode next;
    DNode prev;
    
    DNode(int val) {
        this.val = val;
    }
}

class DoublyLinkedList {
    DNode head;
    DNode tail;
    
    // Insert at beginning: O(1)
    void insertFirst(int val) {
        DNode newNode = new DNode(val);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
    }
    
    // Insert at end: O(1)
    void insertLast(int val) {
        DNode newNode = new DNode(val);
        if (tail == null) {
            head = tail = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }
    }
    
    // Insert at specific position: O(n)
    void insertAt(int index, int val) {
        if (index == 0) {
            insertFirst(val);
            return;
        }
        DNode curr = head;
        for (int i = 0; i < index - 1 && curr != null; i++) {
            curr = curr.next;
        }
        if (curr == null) return;
        
        DNode newNode = new DNode(val);
        newNode.next = curr.next;
        newNode.prev = curr;
        if (curr.next != null) {
            curr.next.prev = newNode;
        }
        curr.next = newNode;
    }
    
    // Delete: O(n)
    void delete(int val) {
        DNode curr = head;
        while (curr != null) {
            if (curr.val == val) {
                if (curr.prev != null) {
                    curr.prev.next = curr.next;
                } else {
                    head = curr.next;
                }
                if (curr.next != null) {
                    curr.next.prev = curr.prev;
                } else {
                    tail = curr.prev;
                }
                return;
            }
            curr = curr.next;
        }
    }
}
```

---

<a name="stacks"></a>
# 3. STACKS (LIFO - Last In First Out)

## Stack Implementation using ArrayList

```java
Stack<Integer> stack = new Stack<>();
Deque<Integer> stack = new ArrayDeque<>();  // Preferred for better performance
```

### Key Methods:
```java
// Push: O(1)
stack.push(5);
stack.addLast(5);  // Deque approach

// Pop: O(1)
int top = stack.pop();
int top = stack.removeLast();  // Deque approach

// Peek: O(1) - view without removing
int top = stack.peek();
int top = stack.peekLast();

// Check empty
stack.isEmpty();
stack.size();

// Search (not recommended)
stack.search(5);  // Returns 1 if at top, -1 if not found
```

### Manual Implementation:
```java
class Stack<T> {
    private LinkedList<T> items = new LinkedList<>();
    
    void push(T item) {
        items.addLast(item);
    }
    
    T pop() {
        if (isEmpty()) throw new EmptyStackException();
        return items.removeLast();
    }
    
    T peek() {
        if (isEmpty()) throw new EmptyStackException();
        return items.getLast();
    }
    
    boolean isEmpty() {
        return items.isEmpty();
    }
    
    int size() {
        return items.size();
    }
}
```

### Common Stack Problems:
```java
// 1. Balanced Parentheses
boolean isBalanced(String s) {
    Stack<Character> stack = new Stack<>();
    Map<Character, Character> pairs = Map.of(')', '(', '}', '{', ']', '[');
    
    for (char c : s.toCharArray()) {
        if (c == '(' || c == '{' || c == '[') {
            stack.push(c);
        } else if (pairs.containsKey(c)) {
            if (stack.isEmpty() || stack.pop() != pairs.get(c)) {
                return false;
            }
        }
    }
    return stack.isEmpty();
}

// 2. Next Greater Element (using stack)
int[] nextGreater(int[] arr) {
    int[] result = new int[arr.length];
    Stack<Integer> stack = new Stack<>();
    
    for (int i = arr.length - 1; i >= 0; i--) {
        while (!stack.isEmpty() && stack.peek() <= arr[i]) {
            stack.pop();
        }
        result[i] = stack.isEmpty() ? -1 : stack.peek();
        stack.push(arr[i]);
    }
    return result;
}

// 3. Largest Rectangle in Histogram (using stack)
int largestRectangleArea(int[] heights) {
    Stack<Integer> stack = new Stack<>();
    int maxArea = 0;
    
    for (int i = 0; i < heights.length; i++) {
        while (!stack.isEmpty() && heights[stack.peek()] > heights[i]) {
            int h = heights[stack.pop()];
            int w = stack.isEmpty() ? i : i - stack.peek() - 1;
            maxArea = Math.max(maxArea, h * w);
        }
        stack.push(i);
    }
    
    while (!stack.isEmpty()) {
        int h = heights[stack.pop()];
        int w = stack.isEmpty() ? heights.length : heights.length - stack.peek() - 1;
        maxArea = Math.max(maxArea, h * w);
    }
    return maxArea;
}
```

---

<a name="queues"></a>
# 4. QUEUES (FIFO - First In First Out)

## Queue Implementations

```java
Queue<Integer> queue = new LinkedList<>();
Deque<Integer> deque = new ArrayDeque<>();  // More efficient
```

### Key Methods:
```java
// Enqueue (add): O(1)
queue.add(5);       // Throws exception if full
queue.offer(5);     // Returns false if full
queue.addLast(5);   // Deque approach

// Dequeue (remove): O(1)
int front = queue.remove();   // Throws if empty
int front = queue.poll();     // Returns null if empty
int front = queue.removeFirst(); // Deque

// Peek: O(1)
int front = queue.peek();     // Returns null if empty
int front = queue.peekFirst(); // Deque

// Size
queue.size();
queue.isEmpty();
```

### Deque (Double-Ended Queue) - Most Versatile:
```java
Deque<Integer> dq = new ArrayDeque<>();

// Front operations
dq.addFirst(1);     // O(1)
dq.removeFirst();   // O(1)
dq.peekFirst();     // O(1)

// Back operations
dq.addLast(5);      // O(1)
dq.removeLast();    // O(1)
dq.peekLast();      // O(1)

// Can use as Stack
dq.addLast(1);
dq.removeLast();

// Or as Queue
dq.addLast(1);
dq.removeFirst();
```

### Common Queue Problems:
```java
// 1. Sliding Window Maximum (using deque)
int[] maxSlidingWindow(int[] nums, int k) {
    int[] result = new int[nums.length - k + 1];
    Deque<Integer> dq = new ArrayDeque<>();
    
    for (int i = 0; i < nums.length; i++) {
        // Remove elements outside window
        if (!dq.isEmpty() && dq.peekFirst() < i - k + 1) {
            dq.removeFirst();
        }
        
        // Remove smaller elements from back
        while (!dq.isEmpty() && nums[dq.peekLast()] < nums[i]) {
            dq.removeLast();
        }
        
        dq.addLast(i);
        
        if (i >= k - 1) {
            result[i - k + 1] = nums[dq.peekFirst()];
        }
    }
    return result;
}

// 2. Binary Tree Level Order (using queue)
List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> result = new ArrayList<>();
    if (root == null) return result;
    
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        List<Integer> level = new ArrayList<>();
        int size = queue.size();
        
        for (int i = 0; i < size; i++) {
            TreeNode node = queue.poll();
            level.add(node.val);
            
            if (node.left != null) queue.offer(node.left);
            if (node.right != null) queue.offer(node.right);
        }
        
        result.add(level);
    }
    return result;
}
```

---

<a name="hash-structures"></a>
# 5. HASH STRUCTURES (Map, Set)

## HashMap

```java
Map<String, Integer> map = new HashMap<>();
Map<String, Integer> map = new HashMap<>(16, 0.75f);  // Initial capacity, load factor
```

### Key Methods:
```java
// Add/Update: O(1) average
map.put("key", 10);
map.putIfAbsent("key", 20);  // Only if absent
map.putAll(otherMap);

// Retrieve: O(1) average
int value = map.get("key");
int value = map.getOrDefault("key", 0);
boolean hasKey = map.containsKey("key");
boolean hasValue = map.containsValue(10);

// Remove: O(1) average
map.remove("key");
map.remove("key", 10);  // Remove if value matches

// Size
map.size();
map.isEmpty();
map.clear();

// Iteration
for (String key : map.keySet()) { }           // Keys: O(n)
for (Integer value : map.values()) { }         // Values: O(n)
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    String key = entry.getKey();
    Integer value = entry.getValue();
}

// Compute
map.compute("key", (k, v) -> v == null ? 1 : v + 1);
map.merge("key", 1, Integer::sum);
map.replaceAll((k, v) -> v * 2);

// Get or default
map.getOrDefault("key", 0);

// Remove if
map.entrySet().removeIf(entry -> entry.getValue() > 10);
```

### Time Complexity:
| Operation | Time |
|-----------|------|
| Put | O(1) average |
| Get | O(1) average |
| Remove | O(1) average |
| Iteration | O(n) |

---

## HashSet

```java
Set<Integer> set = new HashSet<>();
Set<Integer> set = new HashSet<>(16, 0.75f);
Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3));
```

### Key Methods:
```java
// Add: O(1) average
set.add(5);
set.addAll(otherSet);

// Check membership: O(1) average
boolean contains = set.contains(5);

// Remove: O(1) average
set.remove(5);
set.removeIf(x -> x > 10);
set.clear();

// Size
set.size();
set.isEmpty();

// Iteration
for (int x : set) { }
set.forEach(x -> System.out.println(x));

// Set operations
set1.retainAll(set2);   // Intersection: O(min(n1, n2))
set1.addAll(set2);      // Union: O(n2)
set1.removeAll(set2);   // Difference: O(n2)

// To Array
Integer[] arr = set.toArray(new Integer[0]);
```

---

## LinkedHashMap (Insertion Order)

```java
Map<String, Integer> map = new LinkedHashMap<>();  // Maintains insertion order
```

### Usage:
```java
LinkedHashMap<String, Integer> lhm = new LinkedHashMap<>();
lhm.put("first", 1);
lhm.put("second", 2);
lhm.put("third", 3);

// Iteration maintains insertion order
for (String key : lhm.keySet()) {
    System.out.println(key);  // first, second, third
}

// LRU Cache implementation
LinkedHashMap<Integer, Integer> lru = new LinkedHashMap<Integer, Integer>(16, 0.75f, true) {
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > CAPACITY;  // Remove LRU when full
    }
};
```

---

## TreeMap (Sorted by Key)

```java
Map<String, Integer> map = new TreeMap<>();  // Red-Black Tree
Map<Integer, String> map = new TreeMap<>(Collections.reverseOrder());
```

### Key Methods:
```java
// Same as HashMap plus:

// Get closest keys
map.lowerKey(key);           // Largest key < key
map.higherKey(key);          // Smallest key > key
map.floorKey(key);           // Largest key <= key
map.ceilingKey(key);         // Smallest key >= key

// Get range
SortedMap<String, Integer> submap = map.subMap("a", "z");  // [a, z)
SortedMap<String, Integer> headMap = map.headMap("m");     // < "m"
SortedMap<String, Integer> tailMap = map.tailMap("m");     // >= "m"

// First/Last
map.firstKey();
map.lastKey();
map.pollFirstEntry();
map.pollLastEntry();
```

### Time Complexity:
| Operation | Time |
|-----------|------|
| Put | O(log n) |
| Get | O(log n) |
| Remove | O(log n) |
| Range query | O(log n + k) |

---

## TreeSet (Sorted)

```java
Set<Integer> set = new TreeSet<>();
Set<Integer> set = new TreeSet<>(Collections.reverseOrder());
Set<Integer> set = new TreeSet<>(comparator);
```

### Key Methods:
```java
// Same as TreeMap plus:

set.lower(element);          // Largest < element
set.higher(element);         // Smallest > element
set.floor(element);          // Largest <= element
set.ceiling(element);        // Smallest >= element

set.headSet(element);        // All < element
set.tailSet(element);        // All >= element
set.subSet(low, high);       // [low, high)

set.first();
set.last();
set.pollFirst();
set.pollLast();
```

---

## Common HashMap/HashSet Problems:

```java
// 1. Two Sum (using HashMap)
int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[]{map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    return new int[]{};
}

// 2. Longest Substring Without Repeating (using HashMap)
int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> map = new HashMap<>();
    int maxLen = 0;
    int left = 0;
    
    for (int right = 0; right < s.length(); right++) {
        if (map.containsKey(s.charAt(right))) {
            left = Math.max(left, map.get(s.charAt(right)) + 1);
        }
        map.put(s.charAt(right), right);
        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}

// 3. Frequency Map
Map<Integer, Integer> freqMap = new HashMap<>();
for (int num : nums) {
    freqMap.put(num, freqMap.getOrDefault(num, 0) + 1);
}

// Sort by frequency
List<Integer> sorted = freqMap.entrySet().stream()
    .sorted((a, b) -> b.getValue() - a.getValue())
    .map(Map.Entry::getKey)
    .collect(Collectors.toList());
```

---

<a name="trees"></a>
# 6. TREES

## Binary Tree Definition

```java
class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;
    
    TreeNode(int val) {
        this.val = val;
    }
}
```

---

## Binary Search Tree (BST)

```java
class BST {
    TreeNode root;
    
    // Insert: O(log n) average, O(n) worst
    void insert(int val) {
        root = insertHelper(root, val);
    }
    
    private TreeNode insertHelper(TreeNode node, int val) {
        if (node == null) {
            return new TreeNode(val);
        }
        if (val < node.val) {
            node.left = insertHelper(node.left, val);
        } else if (val > node.val) {
            node.right = insertHelper(node.right, val);
        }
        return node;
    }
    
    // Search: O(log n) average, O(n) worst
    boolean search(int val) {
        return searchHelper(root, val);
    }
    
    private boolean searchHelper(TreeNode node, int val) {
        if (node == null) return false;
        if (val == node.val) return true;
        if (val < node.val) return searchHelper(node.left, val);
        return searchHelper(node.right, val);
    }
    
    // Delete: O(log n) average, O(n) worst
    void delete(int val) {
        root = deleteHelper(root, val);
    }
    
    private TreeNode deleteHelper(TreeNode node, int val) {
        if (node == null) return null;
        
        if (val < node.val) {
            node.left = deleteHelper(node.left, val);
        } else if (val > node.val) {
            node.right = deleteHelper(node.right, val);
        } else {
            // Case 1: No children (leaf)
            if (node.left == null && node.right == null) {
                return null;
            }
            // Case 2: One child
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            
            // Case 3: Two children
            // Find minimum in right subtree (inorder successor)
            TreeNode minRight = findMin(node.right);
            node.val = minRight.val;
            node.right = deleteHelper(node.right, minRight.val);
        }
        return node;
    }
    
    private TreeNode findMin(TreeNode node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }
}
```

---

## Tree Traversals

```java
// Inorder (Left, Root, Right) - gives sorted sequence
void inorder(TreeNode node) {
    if (node == null) return;
    inorder(node.left);
    System.out.println(node.val);
    inorder(node.right);
}

// Preorder (Root, Left, Right) - useful for copying
void preorder(TreeNode node) {
    if (node == null) return;
    System.out.println(node.val);
    preorder(node.left);
    preorder(node.right);
}

// Postorder (Left, Right, Root) - useful for deletion
void postorder(TreeNode node) {
    if (node == null) return;
    postorder(node.left);
    postorder(node.right);
    System.out.println(node.val);
}

// Level Order (BFS)
void levelOrder(TreeNode root) {
    if (root == null) return;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.offer(root);
    
    while (!queue.isEmpty()) {
        TreeNode node = queue.poll();
        System.out.println(node.val);
        if (node.left != null) queue.offer(node.left);
        if (node.right != null) queue.offer(node.right);
    }
}
```

---

## Balanced BST (AVL Tree Concept)

```java
// Self-balancing - not in Java Collections, but concept important
// TreeMap uses Red-Black Tree internally

// Red-Black Tree properties:
// 1. Every node is red or black
// 2. Root is black
// 3. All leaves (null) are black
// 4. Red node has black children
// 5. All paths from node to leaves have same black count

// TreeMap guarantees O(log n) for all operations
NavigableMap<Integer, String> map = new TreeMap<>();
map.put(5, "five");
map.put(3, "three");
map.put(7, "seven");

// Guaranteed O(log n) even worst case
System.out.println(map.get(5));      // O(log n)
System.out.println(map.lowerKey(5)); // O(log n)
```

---

## Trie (Prefix Tree)

```java
class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord = false;
}

class Trie {
    TrieNode root = new TrieNode();
    
    // Insert: O(m) where m = word length
    void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEndOfWord = true;
    }
    
    // Search: O(m)
    boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return false;
            }
            node = node.children.get(c);
        }
        return node.isEndOfWord;
    }
    
    // Prefix: O(m)
    boolean startsWith(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return false;
            }
            node = node.children.get(c);
        }
        return true;
    }
    
    // Get all words starting with prefix
    List<String> getAllWords(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = root;
        
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return results;
            }
            node = node.children.get(c);
        }
        
        dfs(node, prefix, results);
        return results;
    }
    
    private void dfs(TrieNode node, String prefix, List<String> results) {
        if (node.isEndOfWord) {
            results.add(prefix);
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            dfs(entry.getValue(), prefix + entry.getKey(), results);
        }
    }
}
```

---

<a name="graphs"></a>
# 7. GRAPHS

## Graph Representation

```java
// Adjacency List (most common)
Map<Integer, List<Integer>> graph = new HashMap<>();
graph.put(0, Arrays.asList(1, 2));
graph.put(1, Arrays.asList(0, 2));
graph.put(2, Arrays.asList(0, 1));

// Adjacency Matrix
int[][] adj = new int[n][n];
adj[0][1] = 1;  // Edge from 0 to 1
adj[1][0] = 1;  // Undirected
```

---

## DFS (Depth-First Search)

```java
// Recursive: O(V + E)
void dfs(int node, Set<Integer> visited, List<Integer>[] graph) {
    visited.add(node);
    System.out.println(node);
    
    for (int neighbor : graph[node]) {
        if (!visited.contains(neighbor)) {
            dfs(neighbor, visited, graph);
        }
    }
}

// Iterative using Stack
void dfsIterative(int start, List<Integer>[] graph) {
    Set<Integer> visited = new HashSet<>();
    Stack<Integer> stack = new Stack<>();
    stack.push(start);
    
    while (!stack.isEmpty()) {
        int node = stack.pop();
        if (visited.contains(node)) continue;
        
        visited.add(node);
        System.out.println(node);
        
        for (int neighbor : graph[node]) {
            if (!visited.contains(neighbor)) {
                stack.push(neighbor);
            }
        }
    }
}
```

---

## BFS (Breadth-First Search)

```java
// O(V + E)
void bfs(int start, List<Integer>[] graph) {
    Set<Integer> visited = new HashSet<>();
    Queue<Integer> queue = new LinkedList<>();
    queue.offer(start);
    visited.add(start);
    
    while (!queue.isEmpty()) {
        int node = queue.poll();
        System.out.println(node);
        
        for (int neighbor : graph[node]) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                queue.offer(neighbor);
            }
        }
    }
}
```

---

## Topological Sort (DAG - Directed Acyclic Graph)

```java
// Using DFS: O(V + E)
void topologicalSort(int node, Set<Integer> visited, Stack<Integer> stack, 
                      List<Integer>[] graph) {
    visited.add(node);
    
    for (int neighbor : graph[node]) {
        if (!visited.contains(neighbor)) {
            topologicalSort(neighbor, visited, stack, graph);
        }
    }
    
    stack.push(node);
}

List<Integer> getTopologicalOrder(List<Integer>[] graph, int n) {
    Set<Integer> visited = new HashSet<>();
    Stack<Integer> stack = new Stack<>();
    
    for (int i = 0; i < n; i++) {
        if (!visited.contains(i)) {
            topologicalSort(i, visited, stack, graph);
        }
    }
    
    List<Integer> result = new ArrayList<>();
    while (!stack.isEmpty()) {
        result.add(stack.pop());
    }
    return result;
}

// Kahn's Algorithm (Using In-Degree): O(V + E)
List<Integer> kahnsAlgorithm(int n, List<Integer>[] graph) {
    int[] inDegree = new int[n];
    
    // Calculate in-degrees
    for (int node = 0; node < n; node++) {
        for (int neighbor : graph[node]) {
            inDegree[neighbor]++;
        }
    }
    
    // BFS from nodes with in-degree 0
    Queue<Integer> queue = new LinkedList<>();
    for (int i = 0; i < n; i++) {
        if (inDegree[i] == 0) {
            queue.offer(i);
        }
    }
    
    List<Integer> result = new ArrayList<>();
    while (!queue.isEmpty()) {
        int node = queue.poll();
        result.add(node);
        
        for (int neighbor : graph[node]) {
            inDegree[neighbor]--;
            if (inDegree[neighbor] == 0) {
                queue.offer(neighbor);
            }
        }
    }
    
    return result;
}
```

---

## Dijkstra's Algorithm (Shortest Path)

```java
// O((V + E) log V) with min-heap
int[] dijkstra(int start, int n, List<int[]>[] graph) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    
    PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> a[0] - b[0]);
    pq.offer(new int[]{0, start});
    
    while (!pq.isEmpty()) {
        int[] curr = pq.poll();
        int d = curr[0];
        int node = curr[1];
        
        if (d > dist[node]) continue;
        
        for (int[] edge : graph[node]) {
            int neighbor = edge[0];
            int weight = edge[1];
            
            if (dist[node] + weight < dist[neighbor]) {
                dist[neighbor] = dist[node] + weight;
                pq.offer(new int[]{dist[neighbor], neighbor});
            }
        }
    }
    
    return dist;
}
```

---

## Bellman-Ford (Handles Negative Weights)

```java
// O(V * E) - slower but handles negative weights
int[] bellmanFord(int start, int n, List<int[]> edges) {
    int[] dist = new int[n];
    Arrays.fill(dist, Integer.MAX_VALUE);
    dist[start] = 0;
    
    // Relax edges V-1 times
    for (int i = 0; i < n - 1; i++) {
        for (int[] edge : edges) {
            int u = edge[0];
            int v = edge[1];
            int weight = edge[2];
            
            if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                dist[v] = dist[u] + weight;
            }
        }
    }
    
    // Check for negative cycles
    for (int[] edge : edges) {
        int u = edge[0];
        int v = edge[1];
        int weight = edge[2];
        
        if (dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
            System.out.println("Negative cycle detected");
            return null;
        }
    }
    
    return dist;
}
```

---

## Union-Find (Disjoint Set Union)

```java
class UnionFind {
    int[] parent;
    int[] rank;
    
    public UnionFind(int n) {
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }
    
    // Find with path compression: O(α(n)) ≈ O(1)
    int find(int x) {
        if (parent[x] != x) {
            parent[x] = find(parent[x]);  // Path compression
        }
        return parent[x];
    }
    
    // Union by rank: O(α(n)) ≈ O(1)
    boolean union(int x, int y) {
        int px = find(x);
        int py = find(y);
        
        if (px == py) return false;
        
        if (rank[px] < rank[py]) {
            parent[px] = py;
        } else if (rank[px] > rank[py]) {
            parent[py] = px;
        } else {
            parent[py] = px;
            rank[px]++;
        }
        return true;
    }
    
    boolean connected(int x, int y) {
        return find(x) == find(y);
    }
}

// Usage - Kruskal's Algorithm (MST)
int kruskalsMST(int n, List<int[]> edges) {
    // edges: [u, v, weight]
    Collections.sort(edges, (a, b) -> a[2] - b[2]);
    
    UnionFind uf = new UnionFind(n);
    int totalWeight = 0;
    int edgesAdded = 0;
    
    for (int[] edge : edges) {
        int u = edge[0];
        int v = edge[1];
        int weight = edge[2];
        
        if (uf.union(u, v)) {
            totalWeight += weight;
            edgesAdded++;
            if (edgesAdded == n - 1) break;
        }
    }
    
    return totalWeight;
}
```

---

<a name="heap-priority-queue"></a>
# 8. HEAP / PRIORITY QUEUE

## PriorityQueue (Min-Heap by default)

```java
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);
```

### Key Methods:
```java
// Add: O(log n)
heap.add(5);
heap.offer(5);

// Remove min: O(log n)
int min = heap.remove();   // Throws if empty
int min = heap.poll();     // Returns null if empty

// Peek min: O(1)
int min = heap.peek();     // Returns null if empty

// Size
heap.size();
heap.isEmpty();

// No index access - O(n) if needed
heap.contains(5);
```

---

## Custom Comparator Heap

```java
// Max heap of integers
PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a, b) -> b - a);

// Min heap of custom objects
class Task implements Comparable<Task> {
    int priority;
    String name;
    
    @Override
    public int compareTo(Task other) {
        return Integer.compare(this.priority, other.priority);
    }
}

PriorityQueue<Task> taskQueue = new PriorityQueue<>();

// Or with custom comparator
PriorityQueue<Task> taskQueue = new PriorityQueue<>(
    (t1, t2) -> Integer.compare(t2.priority, t1.priority)  // Max heap
);
```

---

## Common Heap Problems

```java
// 1. K Largest Elements: O(n log k)
List<Integer> findKLargest(int[] nums, int k) {
    PriorityQueue<Integer> minHeap = new PriorityQueue<>();
    
    for (int num : nums) {
        minHeap.offer(num);
        if (minHeap.size() > k) {
            minHeap.poll();
        }
    }
    
    return new ArrayList<>(minHeap);
}

// 2. Meeting Rooms: O(n log n)
boolean canAttendMeetings(int[][] intervals) {
    Arrays.sort(intervals, (a, b) -> a[0] - b[0]);
    
    for (int i = 1; i < intervals.length; i++) {
        if (intervals[i][0] < intervals[i-1][1]) {
            return false;
        }
    }
    return true;
}

// 3. Merge K Sorted Lists: O(n log k)
class ListNode {
    int val;
    ListNode next;
}

ListNode mergeKLists(ListNode[] lists) {
    PriorityQueue<ListNode> minHeap = new PriorityQueue<>(
        (a, b) -> a.val - b.val
    );
    
    for (ListNode list : lists) {
        if (list != null) {
            minHeap.offer(list);
        }
    }
    
    ListNode dummy = new ListNode();
    ListNode curr = dummy;
    
    while (!minHeap.isEmpty()) {
        ListNode node = minHeap.poll();
        curr.next = node;
        curr = curr.next;
        
        if (node.next != null) {
            minHeap.offer(node.next);
        }
    }
    
    return dummy.next;
}
```

---

<a name="union-find"></a>
# 9. UNION-FIND (Already covered above)

---

<a name="java-collections"></a>
# 10. JAVA COLLECTIONS FRAMEWORK

## Collections Class (Utility Methods)

```java
// Sorting
Collections.sort(list);                          // O(n log n)
Collections.sort(list, comparator);
Collections.reverse(list);                       // O(n)
Collections.shuffle(list);                       // O(n)
Collections.rotate(list, distance);              // O(n)

// Searching
Collections.binarySearch(list, key);             // O(log n)
int index = Collections.binarySearch(
    list, key, comparator
);

// Finding extremes
Collections.max(list);                           // O(n)
Collections.max(list, comparator);
Collections.min(list);
Collections.min(list, comparator);

// Frequency
int freq = Collections.frequency(list, value);   // O(n)

// Replacing
Collections.replaceAll(list, oldVal, newVal);   // O(n)
Collections.fill(list, value);                   // O(n)

// Copying
Collections.copy(dest, src);                     // O(n)

// Unmodifiable/Synchronized collections
Collections.unmodifiableList(list);
Collections.unmodifiableMap(map);
Collections.synchronizedList(list);
Collections.synchronizedMap(map);

// Singletons
Collections.singletonList(value);
Collections.singletonMap(key, value);
Collections.singletonSet(value);

// Empty collections
Collections.emptyList();
Collections.emptyMap();
Collections.emptySet();

// Custom orderings
Collections.reverseOrder();
Collections.reverseOrder(comparator);
```

---

## List Ordering & Hierarchy

```
Collection<E>
    ├── List<E> (ordered, duplicates allowed)
    │   ├── ArrayList (resizable array)
    │   ├── LinkedList (doubly-linked list)
    │   └── CopyOnWriteArrayList (thread-safe)
    │
    ├── Set<E> (unordered, no duplicates)
    │   ├── HashSet
    │   ├── LinkedHashSet (insertion order)
    │   ├── TreeSet (sorted)
    │   └── CopyOnWriteArraySet (thread-safe)
    │
    └── Queue<E> (FIFO + special operations)
        ├── PriorityQueue (heap)
        ├── Deque (double-ended)
        │   └── ArrayDeque
        └── LinkedList (also a Queue)

Map<K,V> (key-value pairs)
├── HashMap
├── LinkedHashMap (insertion order)
├── TreeMap (sorted by key)
├── WeakHashMap
├── IdentityHashMap
└── ConcurrentHashMap (thread-safe)
```

---

<a name="streams-api"></a>
# 11. STREAMS API

## Basic Stream Operations

```java
List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

// Create stream
numbers.stream()                    // From collection
Arrays.stream(array)                // From array
Stream.of(1, 2, 3)                  // From values
Stream.generate(() -> 1)            // Infinite stream
Stream.iterate(0, n -> n + 1)       // Infinite stream
IntStream.range(1, 10)              // Range [1, 10)
IntStream.rangeClosed(1, 10)        // Range [1, 10]
```

### Intermediate Operations (Lazy - return Stream):

```java
// Filter: O(n)
numbers.stream()
    .filter(n -> n > 2)             // Keep only > 2
    .collect(Collectors.toList());
// Result: [3, 4, 5]

// Map: O(n)
numbers.stream()
    .map(n -> n * 2)                // Transform each element
    .collect(Collectors.toList());
// Result: [2, 4, 6, 8, 10]

// FlatMap: O(n)
List<List<Integer>> nested = Arrays.asList(
    Arrays.asList(1, 2),
    Arrays.asList(3, 4)
);
nested.stream()
    .flatMap(List::stream)          // Flatten nested lists
    .collect(Collectors.toList());
// Result: [1, 2, 3, 4]

// Distinct: O(n)
numbers.stream()
    .distinct()                     // Remove duplicates
    .collect(Collectors.toList());

// Sorted: O(n log n)
numbers.stream()
    .sorted()                       // Natural order
    .sorted(Comparator.reverseOrder())  // Reverse
    .collect(Collectors.toList());

// Limit: O(k) where k = limit
numbers.stream()
    .limit(3)                       // Take first 3
    .collect(Collectors.toList());
// Result: [1, 2, 3]

// Skip: O(n)
numbers.stream()
    .skip(2)                        // Skip first 2
    .collect(Collectors.toList());
// Result: [3, 4, 5]

// Peek: O(n) - debugging
numbers.stream()
    .peek(System.out::println)      // Print without consuming
    .filter(n -> n > 2)
    .collect(Collectors.toList());

// Map to int/long/double
numbers.stream()
    .mapToInt(Integer::intValue)
    .sum();
```

### Terminal Operations (Eager - trigger computation):

```java
// Collect: O(n)
numbers.stream()
    .collect(Collectors.toList());
numbers.stream()
    .collect(Collectors.toSet());
numbers.stream()
    .collect(Collectors.toCollection(HashSet::new));

// ForEach: O(n)
numbers.forEach(System.out::println);
numbers.stream()
    .forEach(n -> System.out.println(n * 2));

// Reduce: O(n)
int sum = numbers.stream()
    .reduce(0, (a, b) -> a + b);
int product = numbers.stream()
    .reduce(1, (a, b) -> a * b);

// Count: O(n)
long count = numbers.stream()
    .filter(n -> n > 2)
    .count();

// Find first/any
Optional<Integer> first = numbers.stream()
    .filter(n -> n > 2)
    .findFirst();
Optional<Integer> any = numbers.stream()
    .filter(n -> n > 2)
    .findAny();

// All match / Any match / None match
boolean allPositive = numbers.stream()
    .allMatch(n -> n > 0);
boolean anyGreater5 = numbers.stream()
    .anyMatch(n -> n > 5);
boolean noneNegative = numbers.stream()
    .noneMatch(n -> n < 0);

// Min/Max
Optional<Integer> min = numbers.stream().min(Integer::compare);
Optional<Integer> max = numbers.stream().max(Integer::compare);

// Group by: O(n)
Map<Integer, List<Integer>> byParity = numbers.stream()
    .collect(Collectors.groupingBy(n -> n % 2));
// Result: {0=[2,4], 1=[1,3,5]}

// Partition: O(n)
Map<Boolean, List<Integer>> evenOdd = numbers.stream()
    .collect(Collectors.partitioningBy(n -> n % 2 == 0));
// Result: {false=[1,3,5], true=[2,4]}

// Join to string
String result = numbers.stream()
    .map(String::valueOf)
    .collect(Collectors.joining(", "));
// Result: "1, 2, 3, 4, 5"

// Custom collection: O(n)
Set<Integer> set = numbers.stream()
    .collect(Collectors.toCollection(HashSet::new));

// Custom reduction
int sum = numbers.stream()
    .collect(
        () -> new int[1],
        (acc, n) -> acc[0] += n,
        (acc1, acc2) -> acc1[0] += acc2[0]
    )[0];
```

---

## Advanced Collectors

```java
// Count by group
Map<Integer, Long> countByParity = numbers.stream()
    .collect(Collectors.groupingBy(
        n -> n % 2,
        Collectors.counting()
    ));

// Sum by group
Map<Integer, Integer> sumByParity = numbers.stream()
    .collect(Collectors.groupingBy(
        n -> n % 2,
        Collectors.summingInt(Integer::intValue)
    ));

// Max by group
Map<Integer, Optional<Integer>> maxByParity = numbers.stream()
    .collect(Collectors.groupingBy(
        n -> n % 2,
        Collectors.maxBy(Integer::compareTo)
    ));

// Mapping + Collecting
List<String> stringsByParity = numbers.stream()
    .collect(Collectors.groupingBy(
        n -> n % 2,
        Collectors.mapping(String::valueOf, Collectors.toList())
    ));

// Joining in groups
Map<Integer, String> stringJoinByParity = numbers.stream()
    .collect(Collectors.groupingBy(
        n -> n % 2,
        Collectors.mapping(String::valueOf, Collectors.joining(","))
    ));

// Statistics
IntSummaryStatistics stats = numbers.stream()
    .collect(Collectors.summarizingInt(Integer::intValue));
System.out.println(stats.getSum());
System.out.println(stats.getAverage());
System.out.println(stats.getMin());
System.out.println(stats.getMax());
System.out.println(stats.getCount());
```

---

## Parallel Streams

```java
// Sequential
List<Integer> result = numbers.stream()
    .filter(n -> n > 2)
    .map(n -> n * 2)
    .collect(Collectors.toList());

// Parallel
List<Integer> result = numbers.parallelStream()
    .filter(n -> n > 2)
    .map(n -> n * 2)
    .collect(Collectors.toList());

// Convert to parallel
List<Integer> result = numbers.stream()
    .parallel()
    .filter(n -> n > 2)
    .map(n -> n * 2)
    .collect(Collectors.toList());

// Convert back to sequential
List<Integer> result = numbers.stream()
    .parallel()
    .filter(n -> n > 2)
    .sequential()
    .map(n -> n * 2)
    .collect(Collectors.toList());

// Performance
// Use parallel only for:
// - Large collections (1000+)
// - Expensive operations
// - Not for small collections (overhead > benefit)
```

---

<a name="comparable-comparator"></a>
# 12. COMPARABLE & COMPARATOR

## Comparable Interface

```java
class User implements Comparable<User> {
    String name;
    int age;
    
    @Override
    public int compareTo(User other) {
        // Natural ordering: by age ascending
        return Integer.compare(this.age, other.age);
    }
}

// Usage
List<User> users = new ArrayList<>();
Collections.sort(users);  // Uses compareTo()
```

---

## Comparator Interface

```java
// Lambda comparator
Comparator<User> byAge = (u1, u2) -> Integer.compare(u1.age, u2.age);
Comparator<User> byName = (u1, u2) -> u1.name.compareTo(u2.name);

// Method reference comparator
Comparator<User> byAge2 = Comparator.comparingInt(u -> u.age);

// Built-in comparators
Comparator<Integer> ascending = Integer::compareTo;
Comparator<Integer> descending = (a, b) -> b - a;

// Comparator builder
Comparator<User> multiLevel = Comparator
    .comparingInt(User::getAge)          // Primary: age ascending
    .thenComparing(User::getName);       // Secondary: name ascending

Comparator<User> multiLevelReverse = Comparator
    .comparingInt(User::getAge).reversed()
    .thenComparing(User::getName);

// Sorting with Comparator
Collections.sort(users, byName);
users.sort(byAge);
users.stream().sorted(byName).collect(Collectors.toList());

// Reusable comparators
class Comparators {
    public static <T> Comparator<T> reverse(Comparator<T> c) {
        return c.reversed();
    }
    
    public static <T> Comparator<T> nullsLast(Comparator<T> c) {
        return Comparator.nullsLast(c);
    }
}
```

---

<a name="common-patterns"></a>
# 13. COMMON PATTERNS & TECHNIQUES

## Sliding Window

```java
// Fixed window
int maxSumFixedWindow(int[] nums, int k) {
    int sum = 0;
    for (int i = 0; i < k; i++) sum += nums[i];
    int maxSum = sum;
    
    for (int i = k; i < nums.length; i++) {
        sum = sum - nums[i - k] + nums[i];
        maxSum = Math.max(maxSum, sum);
    }
    return maxSum;
}

// Variable window
int maxLengthSubarray(int[] nums) {
    int left = 0;
    int maxLen = 0;
    Set<Integer> window = new HashSet<>();
    
    for (int right = 0; right < nums.length; right++) {
        while (window.contains(nums[right])) {
            window.remove(nums[left++]);
        }
        window.add(nums[right]);
        maxLen = Math.max(maxLen, right - left + 1);
    }
    return maxLen;
}
```

---

## Two Pointers

```java
// Merge two sorted arrays
void merge(int[] nums1, int m, int[] nums2, int n) {
    int p1 = m - 1;
    int p2 = n - 1;
    int p = m + n - 1;
    
    while (p1 >= 0 && p2 >= 0) {
        if (nums1[p1] > nums2[p2]) {
            nums1[p--] = nums1[p1--];
        } else {
            nums1[p--] = nums2[p2--];
        }
    }
    
    while (p2 >= 0) {
        nums1[p--] = nums2[p2--];
    }
}

// Remove duplicates
int removeDuplicates(int[] nums) {
    int slow = 0;
    for (int fast = 1; fast < nums.length; fast++) {
        if (nums[fast] != nums[slow]) {
            slow++;
            nums[slow] = nums[fast];
        }
    }
    return slow + 1;
}
```

---

## Binary Search

```java
// Standard
int binarySearch(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;
    
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) {
            return mid;
        } else if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    return -1;
}

// Find first position
int findFirst(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;
    int result = -1;
    
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) {
            result = mid;
            right = mid - 1;  // Keep searching left
        } else if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    return result;
}

// Find last position
int findLast(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;
    int result = -1;
    
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (nums[mid] == target) {
            result = mid;
            left = mid + 1;  // Keep searching right
        } else if (nums[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }
    return result;
}

// Find closest
int findClosest(int[] nums, int target) {
    int left = 0;
    int right = nums.length - 1;
    
    while (right - left > 1) {
        int mid = left + (right - left) / 2;
        if (nums[mid] < target) {
            left = mid;
        } else {
            right = mid;
        }
    }
    
    if (Math.abs(nums[left] - target) < Math.abs(nums[right] - target)) {
        return nums[left];
    }
    return nums[right];
}
```

---

## DFS vs BFS Decision

```
DFS (Stack):
- Tree/Graph traversal
- Cycle detection
- Path finding (deep)
- Backtracking
- Memory efficient for deep trees
- LIFO order

BFS (Queue):
- Shortest path (unweighted)
- Level-order traversal
- Closest element
- Connected components
- Memory intensive for wide trees
- FIFO order
```

---

<a name="time-complexity"></a>
# 14. TIME COMPLEXITY CHEAT SHEET

## Data Structure Operations

```
┌──────────────────────────┬──────────┬──────────┬──────────┐
│ Data Structure           │ Access   │ Search   │ Insert   │
├──────────────────────────┼──────────┼──────────┼──────────┤
│ Array                    │ O(1)     │ O(n)     │ O(n)     │
│ Sorted Array             │ O(1)     │ O(log n) │ O(n)     │
│ LinkedList               │ O(n)     │ O(n)     │ O(1)*    │
│ Doubly LinkedList        │ O(n)     │ O(n)     │ O(1)*    │
│ Stack                    │ O(n)     │ O(n)     │ O(1)     │
│ Queue                    │ O(n)     │ O(n)     │ O(1)     │
│ Binary Search Tree       │ O(log n) │ O(log n) │ O(log n) │
│ Balanced BST (AVL/RB)    │ O(log n) │ O(log n) │ O(log n) │
│ Hash Table               │ N/A      │ O(1)*    │ O(1)*    │
│ HashMap                  │ N/A      │ O(1)*    │ O(1)*    │
│ TreeMap                  │ N/A      │ O(log n) │ O(log n) │
│ Heap                     │ O(n)     │ O(n)     │ O(log n) │
│ Trie                     │ N/A      │ O(m)***  │ O(m)***  │
│ Graph (Adjacency List)   │ N/A      │ O(V+E)   │ O(1)     │
└──────────────────────────┴──────────┴──────────┴──────────┘

* = Average case, can be O(n) worst case
** = Or O(n) for deletion
*** = m = length of key/word
```

## Algorithm Complexities

```
Searching:
- Linear Search: O(n)
- Binary Search: O(log n)
- Breadth-First Search (BFS): O(V + E)
- Depth-First Search (DFS): O(V + E)

Sorting:
- Bubble Sort: O(n²)
- Insertion Sort: O(n²)
- Selection Sort: O(n²)
- Merge Sort: O(n log n)
- Quick Sort: O(n log n) avg, O(n²) worst
- Heap Sort: O(n log n)
- Counting Sort: O(n + k)
- Radix Sort: O(nk)

Graph Algorithms:
- Dijkstra: O((V + E) log V)
- Bellman-Ford: O(V × E)
- Floyd-Warshall: O(V³)
- Topological Sort: O(V + E)
- Kruskal's (MST): O(E log E)
- Prim's (MST): O(V²)

Hashing:
- Hash Table Insert: O(1) avg
- Hash Table Search: O(1) avg
- Hash Table Delete: O(1) avg
- Collision resolution (chaining): O(1 + load factor)
```

---

<a name="when-to-use-what"></a>
# 15. WHEN TO USE WHAT

## Data Structure Selection Guide

```
NEED ORDERED COLLECTION?
├─ YES → Want fast insertion/deletion at ends?
│   ├─ YES → LinkedList or Deque
│   └─ NO → ArrayList
└─ NO → Need key-value mapping?
    ├─ YES → Need sorted?
    │   ├─ YES → TreeMap
    │   └─ NO → HashMap or LinkedHashMap
    └─ NO → Need unique values?
        ├─ YES → Need sorted?
        │   ├─ YES → TreeSet
        │   └─ NO → HashSet
        └─ NO → ArrayList

NEED UNIQUE VALUES?
├─ YES → Need order?
│   ├─ Sorted → TreeSet: O(log n) insert/delete
│   ├─ Insertion order → LinkedHashSet
│   └─ Unordered → HashSet: O(1) insert/delete
└─ NO → Use ArrayList

NEED FAST LOOKUP?
├─ HashMap: O(1) average
├─ TreeMap: O(log n)
└─ Array: O(n)

NEED ORDERING?
├─ By insertion → LinkedHashMap, LinkedHashSet
├─ By value → TreeMap, TreeSet with custom comparator
└─ Not needed → HashMap, HashSet

NEED STACK?
├─ Stack<T> (legacy, Vector-based)
├─ Deque<T> (ArrayDeque preferred, faster)
└─ LinkedList (good but slower than ArrayDeque)

NEED QUEUE?
├─ Queue<T> = LinkedList
├─ PriorityQueue: O(log n) insert/remove (min-heap)
├─ Deque: Double-ended queue
└─ Blocking operations → LinkedBlockingQueue (concurrent)

NEED HEAP?
├─ Min-heap: PriorityQueue (default)
├─ Max-heap: PriorityQueue with reversed comparator
├─ Operations: O(log n) for add/remove

WORKING WITH STRINGS?
├─ StringBuilder (not synchronized)
├─ StringBuffer (synchronized, slower)
└─ String (immutable, use for constants)

MULTI-THREADED ENVIRONMENT?
├─ ConcurrentHashMap (better than synchronized HashMap)
├─ CopyOnWriteArrayList (for mostly read, rare write)
├─ Collections.synchronizedList (simple synchronization)
└─ Queue: LinkedBlockingQueue

BIG DATA?
├─ Parallel Streams: parallelStream() - use for 1000+ elements
├─ Minimize intermediate objects: use flatMap carefully
└─ Use primitive streams: IntStream, LongStream, DoubleStream
```

---

## Problem → Solution Mapping

```
Problem: Find two numbers that sum to target
Solution: HashMap for O(n) time

Problem: Find all pairs that sum to target
Solution: Sorted array + two pointers for O(n log n)

Problem: Find K smallest/largest elements
Solution: Min/Max heap with K size for O(n log K)

Problem: Find shortest path in unweighted graph
Solution: BFS for O(V + E)

Problem: Find shortest path with weighted graph
Solution: Dijkstra for O((V + E) log V)

Problem: Find strongly connected components
Solution: DFS (Tarjan or Kosaraju) for O(V + E)

Problem: Validate BST
Solution: Inorder traversal should be sorted

Problem: Find LCA (Lowest Common Ancestor)
Solution: Recursive DFS or parent pointers

Problem: Sliding window maximum
Solution: Deque to maintain decreasing order

Problem: Implement LRU Cache
Solution: HashMap + DoublyLinkedList or LinkedHashMap

Problem: Next greater/smaller element
Solution: Stack to maintain decreasing/increasing order

Problem: All paths from root to leaf
Solution: DFS backtracking

Problem: Topological sort
Solution: DFS with stack or Kahn's algorithm

Problem: Number of islands
Solution: DFS/BFS or Union-Find

Problem: Median of stream
Solution: Two heaps (max-heap for left, min-heap for right)

Problem: Range sum queries
Solution: Segment tree or Fenwick tree (advanced)

Problem: Anagram problems
Solution: HashMap of character frequencies or sorting

Problem: Merge intervals
Solution: Sort by start, merge overlapping ranges
```

---

# CHEAT SHEET SUMMARY

## Most Used Operations:

```java
// Array
Arrays.sort(arr);
Arrays.binarySearch(arr, key);
Arrays.copyOf(arr, length);

// ArrayList
list.add(element);      // O(1) amortized
list.get(index);        // O(1)
list.remove(index);     // O(n)

// HashMap
map.put(key, value);    // O(1)
map.get(key);          // O(1)
map.containsKey(key);  // O(1)

// HashSet
set.add(element);      // O(1)
set.contains(element); // O(1)
set.remove(element);   // O(1)

// TreeMap/TreeSet
map.put(key, value);   // O(log n)
map.get(key);         // O(log n)

// Stack
stack.push(element);   // O(1)
stack.pop();          // O(1)
stack.peek();         // O(1)

// Queue
queue.offer(element); // O(1)
queue.poll();        // O(1)
queue.peek();        // O(1)

// PriorityQueue
pq.offer(element);   // O(log n)
pq.poll();          // O(log n)
pq.peek();          // O(1)

// Streams
list.stream()
    .filter(...)
    .map(...)
    .collect(...);
```

---

**END OF DSA CHEAT SHEET**

This is your complete reference. Bookmark it, study it, master it.
