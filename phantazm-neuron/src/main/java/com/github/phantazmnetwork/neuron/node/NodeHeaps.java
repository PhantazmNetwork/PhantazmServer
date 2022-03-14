package com.github.phantazmnetwork.neuron.node;

import java.util.Comparator;

/**
 * Utility method providing binary min-heap operations on {@link Node} arrays.
 * @see it.unimi.dsi.fastutil.objects.ObjectHeaps
 * @see NodeQueue
 */
public final class NodeHeaps {
    private static class NodeComparator implements Comparator<Node> {
        @Override
        public int compare(Node first, Node second) {
            int fCompare = Float.compare(first.getF(), second.getF());
            if(fCompare == 0) {
                //use natural ordering as tiebreak: should never return 0 during actual pathfinding
                return first.compareTo(second);
            }

            return fCompare;
        }
    }

    /**
     * The comparator used to order the heap in {@link NodeHeaps#downHeap(Node[], int, int)} and
     * {@link NodeHeaps#upHeap(Node[], int)}. Nodes are compared first by {@code f}-score, and then by their natural
     * ordering. This comparator imposes an ordering that is inconsistent with equals.
     * @see Node
     */
    public static final Comparator<Node> NODE_SCORE_COMPARATOR = new NodeComparator();

    /**
     * Method based on {@link it.unimi.dsi.fastutil.objects.ObjectHeaps#downHeap(Object[], int, int, Comparator)}, but
     * specialized for {@link Node} objects. This will ensure that the heap index of all moved nodes is updated to match
     * its position in the actual array. Nodes are always compared using {@link NodeHeaps#NODE_SCORE_COMPARATOR}.
     * @param heap the min-heap of nodes
     * @param size the size of the heap
     * @param i the index of the node to potentially move down
     */
    public static void downHeap(Node[] heap, int size, int i) {
        Node first = heap[i];
        int child;
        while ((child = (i << 1) + 1) < size) {
            Node childNode = heap[child];
            int right = child + 1;
            if (right < size && (NODE_SCORE_COMPARATOR.compare(heap[right], childNode) < 0)) {
                childNode = heap[child = right];
            }

            if (NODE_SCORE_COMPARATOR.compare(first, childNode) <= 0) {
                break;
            }

            heap[i] = childNode;
            childNode.setHeapIndex(i);
            i = child;
        }
        heap[i] = first;
        first.setHeapIndex(i);
    }

    /**
     * Method based on {@link it.unimi.dsi.fastutil.objects.ObjectHeaps#upHeap(Object[], int, int, Comparator)}, but
     * specialized for {@link Node} objects. This will ensure that the heap index of all moved nodes is updated to match
     * its position in the actual array. Nodes are always compared using {@link NodeHeaps#NODE_SCORE_COMPARATOR}.
     * @param heap the min-heap of nodes
     * @param i the index of the node to potentially move up
     * @return the final index of the node which was moved
     */
    public static int upHeap(Node[] heap, int i) {
        Node e = heap[i];
        while (i != 0) {
            int parent = (i - 1) >>> 1;
            Node t = heap[parent];
            if (NODE_SCORE_COMPARATOR.compare(t, e) <= 0) {
                break;
            }

            heap[i] = t;
            t.setHeapIndex(i);
            i = parent;
        }
        heap[i] = e;
        e.setHeapIndex(i);
        return i;
    }
}
