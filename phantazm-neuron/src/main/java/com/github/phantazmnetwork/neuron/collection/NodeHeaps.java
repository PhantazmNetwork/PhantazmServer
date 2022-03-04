package com.github.phantazmnetwork.neuron.collection;

import com.github.phantazmnetwork.neuron.operation.Node;

import java.util.Comparator;

/**
 * Utility method providing binary min-heap operations on {@link Node} arrays.
 * @see it.unimi.dsi.fastutil.objects.ObjectHeaps
 * @see NodeQueue
 */
public final class NodeHeaps {
    /**
     * Method based on {@link it.unimi.dsi.fastutil.objects.ObjectHeaps#downHeap(Object[], int, int, Comparator)}, but
     * specialized for {@link Node} objects. This will ensure that the heap index of all moved nodes is updated to match
     * its position in the actual array.
     * @param heap the min-heap of nodes
     * @param size the size of the heap
     * @param i the index of the node to potentially move down
     */
    public static void downHeap(Node[] heap, int size, int i) {
        Node e = heap[i];
        int child;
        while ((child = (i << 1) + 1) < size) {
            Node t = heap[child];
            int right = child + 1;
            if (right < size && ((heap[right]).compareTo(t) < 0)) {
                t = heap[child = right];
            }

            if ((e.compareTo(t) <= 0)) {
                break;
            }

            heap[i] = t;
            t.setHeapIndex(i);
            i = child;
        }
        heap[i] = e;
        e.setHeapIndex(i);
    }

    /**
     * Method based on {@link it.unimi.dsi.fastutil.objects.ObjectHeaps#upHeap(Object[], int, int, Comparator)}, but
     * specialized for {@link Node} objects. This will ensure that the heap index of all moved nodes is updated to match
     * its position in the actual array.
     * @param heap the min-heap of nodes
     * @param i the index of the node to potentially move up
     * @return the final index of the node which was moved
     */
    public static int upHeap(Node[] heap, int i) {
        Node e = heap[i];
        while (i != 0) {
            int parent = (i - 1) >>> 1;
            Node t = heap[parent];
            if (t.compareTo(e) <= 0) {
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
