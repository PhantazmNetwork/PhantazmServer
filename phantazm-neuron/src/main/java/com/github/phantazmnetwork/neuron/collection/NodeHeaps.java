package com.github.phantazmnetwork.neuron.collection;

import com.github.phantazmnetwork.neuron.operation.Node;

/**
 * Utility method providing binary min-heap operations on {@link Node} arrays.
 * @see it.unimi.dsi.fastutil.objects.ObjectHeaps
 * @see NodeQueue
 */
public final class NodeHeaps {
    public static int downHeap(Node[] heap, int size, int i) {
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
        return i;
    }

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
