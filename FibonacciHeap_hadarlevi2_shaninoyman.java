import java.util.Arrays;


// Hadar Levi , hadarlevi2 , 209006360
// Shani Noyman , shaninoyman , 208660654

/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 */
public class FibonacciHeap {
    public HeapNode firstTreeRoot; //first (left most) node in heap
    public HeapNode min; //node with minimum key in heap
    public int size; //number of nodes in heap
    public static int linksCounter = 0; //counter for totalLinks()
    public static int cutsCounter = 0; //counter for totalCuts()
    public int markedCount = 0; //number of marked nodes in heap
    public int treesCount = 0; //number of trees in heap


    //FibonacciHeap constructor for empty heap
    public FibonacciHeap() {
        this.firstTreeRoot = null;
        this.min = null;
        this.size = 0;
    }


    /**
     * public boolean isEmpty()
     * <p>
     * precondition: none
     * <p>
     * The method returns true if and only if the heap
     * is empty.
     */
    public boolean isEmpty() {
        return this.size == 0; //returns true if there are no trees in heap
    }

    /**
     * public HeapNode insert(int key)
     * <p>
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * <p>
     * Returns the new node created.
     */
    public HeapNode insert(int key) {
        HeapNode newNode = new HeapNode(key); //creating a new node to insert the heap
        if (this.isEmpty()) { //newNode is the only node in heap- the first and min
            this.firstTreeRoot = newNode;
            this.min = newNode;
        }
        else { //insert newNode to the left to be the first node
            newNode.prev = this.firstTreeRoot.prev;
            newNode.next = this.firstTreeRoot;
            this.firstTreeRoot.prev.next = newNode;
            this.firstTreeRoot.prev = newNode;
            this.firstTreeRoot = newNode;


            if (newNode.getKey() < this.min.getKey()) //if newNode is smaller than the current min
                this.min = newNode;
        }
        this.size++; //a node added to the heap
        this.treesCount++; //an one-node-tree tree added to the heap
        return newNode; //returns the inserted node
    }

    /**
     * public void deleteMin()
     * <p>
     * Delete the node containing the minimum key.
     */
    public void deleteMin() {
        if (this.size == 1) { //no need to rebuild (successiveLinking()) the heap- empty after deletion
            this.firstTreeRoot = null;
            this.min = null;
            this.size--;
            this.treesCount--;
            return;
        }
        if (!this.isEmpty()) {
            HeapNode child = this.min.child; //min node's children will be roots before rebuilding heap (successiveLinking())
            if (this.firstTreeRoot.key == this.min.key) { //if min node was also first root
                if (this.firstTreeRoot.next.key == this.firstTreeRoot.key) //if min was the only root in heap
                    this.firstTreeRoot = this.firstTreeRoot.child; //first is min's child
                else //min wasn't the only root in heap
                    this.firstTreeRoot = this.firstTreeRoot.next; //first is min's next
            }
            if (child == null) { //min had no children- there are no new roots to add to heap
                //connecting min.next with min.prev
                this.min.next.prev = this.min.prev;
                this.min.prev.next = this.min.next;

                //disconnecting min from heap
                this.min.next = null;
                this.min.prev = null;

            }
            else if (child != null) { //min had children

                //adding first child as a root to heap
                child.prev.next = this.min.next;
                this.min.next.prev = child.prev;

                child.prev = this.min.prev;
                this.min.prev.next = child;

                //disconnecting min
                this.min.next = null;
                this.min.prev = null;

                //setting all children's parent to null because they are roots now  
                while (child.parent != null) {
                    child.parent = null;
                    child = child.next;
                }
                this.min.child = null;
            }

            this.size--;
            this.min = this.firstTreeRoot; //temporary min- will change while rebuilding the heap (successiveLinking())

            if (this.size > 0) //rebuild heap only if there are tree roots in it
                this.successiveLinking();

        }

    }


    //link two trees (x, y are tree roots) with the same rank
    public HeapNode link(HeapNode x, HeapNode y) {
        if (x.getKey() > y.getKey()) { //setting x to be the smaller key
            HeapNode tmp = x;
            x = y;
            y = tmp;
        }

        //nodes ranks are 0- x, y have no children
        if (x.rank == 0) {
            x.child = y;
            y.parent = x;
        }

        //x.rank != 0
        else {
            y.next = x.child;
            y.prev = x.child.prev;
            x.child.prev.next = y;
            x.child.prev = y;
            x.child = y;
            y.parent = x;
        }

        x.rank++; //a child added to x
        linksCounter++; //one link has been done

        return x; //returns the new tree's root- x
    }

    //rebuilding the heap to be a legal (non lazy) binomial heap
    public void successiveLinking() {
        //array in the length of the maximal optional rank
        HeapNode[] ranksArray = new HeapNode[(int) (Math.log(this.size) / Math.log(2)) + 2];

        HeapNode node = this.firstTreeRoot;
        HeapNode nodeNext = node.next;


        this.firstTreeRoot.prev.next = null; //delete last node's next pointer - in favor of ending the while loop

        while (node.next != null) {
            //disconnecting node
            node.next = node;
            node.prev = node;

            //if there are two trees with the same rank- link them and removing them from ranksArray
            while (ranksArray[node.rank] != null) {
                int lastRank = node.rank;
                node = link(node, ranksArray[node.rank]);
                ranksArray[lastRank] = null;
            }

            //insert the single tree with a specific rank to its place in ranksArray
            ranksArray[node.rank] = node;

            node = nodeNext;
            if (node.next != null)
                nodeNext = node.next;
        }

        //handling last root in heap- same as in the while loop
        node.next = node;
        node.prev = node;

        while (ranksArray[node.rank] != null) {
            int lastRank = node.rank;
            node = link(node, ranksArray[node.rank]);
            ranksArray[lastRank] = null;
        }

        ranksArray[node.rank] = node;

        this.firstTreeRoot = null;

        //initializing and then calculating (in for loop) treesCount
        this.treesCount = 0;
        for (HeapNode root : ranksArray) { //inserting all roots after successive linking to the heap
            if (root != null) {
                this.treesCount++;
                if (this.firstTreeRoot == null) {
                    this.firstTreeRoot = root;
                    this.firstTreeRoot.next = this.firstTreeRoot;
                    this.firstTreeRoot.prev = this.firstTreeRoot;
                }

                this.firstTreeRoot.prev.next = root;
                root.prev = this.firstTreeRoot.prev;

                this.firstTreeRoot.prev = root;
                root.next = this.firstTreeRoot;

                if (root.getKey() < this.min.getKey())
                    this.min = root;
            }
        }
    }


    /**
     * public HeapNode findMin()
     * <p>
     * Return the node of the heap whose key is minimal.
     */
    public HeapNode findMin() {
        return this.min;
    }

    /**
     * public void meld (FibonacciHeap heap2)
     * <p>
     * Meld the heap with heap2
     */
    public void meld(FibonacciHeap heap2) {
        if ((heap2.isEmpty() && this.isEmpty()) || heap2.isEmpty())
            return;
        else if (this.isEmpty()) { //current heap is empty but heap2 is not
            //setting all this's fields to be heap2 fields
            this.firstTreeRoot = heap2.firstTreeRoot;
            this.min = heap2.min;
            this.size = heap2.size;
            this.markedCount = heap2.markedCount;
            this.treesCount = heap2.treesCount;
        }
        else { //both heaps are not empty- adding all heap2 root to the right of this's roots. updating all fields
            HeapNode heap2LastNode = heap2.firstTreeRoot.prev;
            this.firstTreeRoot.prev.next = heap2.firstTreeRoot;
            heap2LastNode.next = this.firstTreeRoot;
            heap2.firstTreeRoot.prev = this.firstTreeRoot.prev;
            this.firstTreeRoot.prev = heap2LastNode;

            this.size += heap2.size;
            this.treesCount += heap2.treesCount;

            if (heap2.min.key < this.min.key) //update min if necessary
                this.min = heap2.min;
        }
    }

    /**
     * public int size()
     * <p>
     * Return the number of elements in the heap
     */
    public int size() {
        return this.size;
    }

    /**
     * public int[] countersRep()
     * <p>
     * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap.
     */
    public int[] countersRep() {
        int[] arr = new int[this.size];
        HeapNode node = this.firstTreeRoot;
        while (node.key != this.firstTreeRoot.prev.key) //all roots besides last
        {
            arr[node.rank]++; //add 1 to the node.rank-th entry
            node = node.next;
        }
        arr[this.firstTreeRoot.prev.rank]++; //last root
        return arr;
    }

    /**
     * public void delete(HeapNode x)
     * <p>
     * Deletes the node x from the heap.
     */
    public void delete(HeapNode x) {
        //decrease x's key to the minimum possible value and than delete using deleteMin
        int MIN_INT = Integer.MIN_VALUE;
        this.decreaseKey(x, x.key - MIN_INT);
        this.deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     * <p>
     * The function decreases the key of the node x by delta. The structure of the heap should be updated
     * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
     */
    public void decreaseKey(HeapNode x, int delta) {
        x.key = x.key - delta; //decreasing x's key by delta

        if (x.parent != null) { //x is not root
            if (x.key < x.parent.key) //violates the heap rule
                cascadingCut(x, x.parent);
        }
        //check if new key is the minimal key
        if (x.key < this.min.key)
            this.min = x;
    }

    //cuts x from y
    public void cut(HeapNode x, HeapNode y) {
        x.parent = null;
        if (x.mark == 1) {
            this.markedCount--;
            x.mark = 0;
        }
        y.rank--;
        if (x.next == x) //x has no siblings
            y.child = null;
        else { //x has siblings
            if (y.child.key == x.key)
                y.child = x.next;
            x.prev.next = x.next;
            x.next.prev = x.prev;
        }
        cutsCounter++;
    }

    //cuts x from y, adds it as a root to heap and handling parent if it was marked
    public void cascadingCut(HeapNode x, HeapNode y) {
        cut(x, y);
        addRootToBeginning(x);
        if (y.parent != null) {
            if (y.mark == 0) {
                y.mark = 1;
                this.markedCount++;
            }
            else //if parent was marked- cuts it from its parent in recursive until parent is unmarked
                cascadingCut(y, y.parent);
        }

    }


    //adds node as a root to the beginning of the heap
    public void addRootToBeginning(HeapNode node) {
        node.prev = this.firstTreeRoot.prev;
        this.firstTreeRoot.prev.next = node;
        this.firstTreeRoot.prev = node;
        node.next = this.firstTreeRoot;
        this.firstTreeRoot = node;

        this.treesCount++;


    }


    /**
     * public int potential()
     * <p>
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap.
     */
    public int potential() {
        return this.treesCount + 2 * this.markedCount;
    }

    /**
     * public static int totalLinks()
     * <p>
     * This static function returns the total number of link operations made during the run-time of the program.
     * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of
     * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value
     * in its root.
     */
    public static int totalLinks() {
        return linksCounter;
    }

    /**
     * public static int totalCuts()
     * <p>
     * This static function returns the total number of cut operations made during the run-time of the program.
     * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods).
     */
    public static int totalCuts() {
        return cutsCounter;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     * <p>
     * This static function returns the k minimal elements in a binomial tree H.
     * The function should run in O(k*deg(H)).
     * You are not allowed to change H.
     */
    public static int[] kMin(FibonacciHeap H, int k) {
        if (H.size == 0)
            return new int[0];
        FibonacciHeap helpHeap = new FibonacciHeap(); //all candidates to be in the result array will be inserted to helpHeap
        int[] arr = new int[k]; //result array
        helpHeap.insert(H.findMin().getKey()); //inserting min to helpHeap
        helpHeap.findMin().pointer = H.findMin(); //setting pointer to the same node in H
        HeapNode tmpNode, insertedNode;
        int tmpKey;

        for (int i = 0; i < k; i++) {
            arr[i] = helpHeap.findMin().getKey(); //insert helpHeap's min key to the result array
            tmpNode = helpHeap.findMin().pointer; //setting tmpNode to be the node in H with helpHeap's minimum key
            if (tmpNode.child != null) { //tmpNode has children
                //inserting all tmpNode's children to helpHeap because they are
                //candidates to be the next minimum key that needs to be in result array
                tmpNode = tmpNode.child;
                insertedNode = helpHeap.insert(tmpNode.getKey()); //inserting tmpNode first child's key to helpHeap
                insertedNode.pointer = tmpNode; //setting insertedNode's pointer to the node in H with the same key
                tmpKey = insertedNode.getKey();
                tmpNode = tmpNode.next;
                while (tmpNode.getKey() != tmpKey) { //inserting all siblings and setting their pointers
                    insertedNode = helpHeap.insert(tmpNode.getKey());
                    insertedNode.pointer = tmpNode;
                    tmpNode = tmpNode.next;
                }
            }
            helpHeap.deleteMin(); //deleteMin from helpHeap after all new candidates to be next minimum are in helpHeap
        }
        return arr;


    }


    /**
     * public class HeapNode
     * <p>
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in
     * another file
     */
    public class HeapNode {
        public int key;
        public int rank;
        public int mark; //1 = marked, 0 = not marked
        public HeapNode child; //pointer to its child
        public HeapNode next; //pointer to its next node
        public HeapNode prev; //pointer to its previous node
        public HeapNode parent; //pointer to its parent
        public HeapNode pointer; //used only in kMin()- pointer to the node with same key in H

        //constructor for HeapNode
        public HeapNode(int key) {
            this.key = key;
            this.rank = 0; //has no children so its rank is 0
            this.mark = 0; //unmarked at first
            this.child = null;
            this.next = this; //points to itself, not part of a heap yet
            this.prev = this; //points to itself, not part of a heap yet
            this.parent = null;
            this.pointer = null;
        }

        //returns its key
        public int getKey() {
            return this.key;
        }
    }
}
