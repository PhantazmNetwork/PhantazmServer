package com.github.phantazmnetwork.neuron.node;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

import java.util.Comparator;
import java.util.NoSuchElementException;

/**
 * A specialized implementation of {@link PriorityQueue} designed with {@link Node} objects in mind. Structurally
 * modifying functions will update the {@code Node} object's internal index using {@link Node#setHeapIndex(int)}. This
 * is used to implement O(1) {@code contains} and faster updating of {@code Node} objects whose comparison relative to
 * other elements in the queue may have changed.
 * @see NodeHeaps
 * @see PriorityQueue
 */
public class NodeQueue implements PriorityQueue<Node> {
    private static final int DEFAULT_INITIAL_CAPACITY = 32;

    private Node[] heap;
    private int size;

    /**
     * Creates a new NodeQueue using the default initial capacity (32).
     */
    public NodeQueue() {
        this.heap = new Node[DEFAULT_INITIAL_CAPACITY];
    }

    /**
     * Creates a new NodeQueue with the provided initial capacity.
     * @param initialCapacity the initial capacity
     * @throws IllegalArgumentException if initialCapacity is negative
     */
    public NodeQueue(int initialCapacity) {
        if(initialCapacity < 0) {
            throw new IllegalArgumentException("Invalid capacity " + initialCapacity);
        }

        if(initialCapacity == 0) {
            heap = (Node[])ObjectArrays.EMPTY_ARRAY;
        }
        else {
            heap = new Node[initialCapacity];
        }
    }

    @Override
    public void enqueue(Node x) {
        if (size == heap.length) {
            heap = ObjectArrays.grow(heap, size + 1);
        }

        heap[size++] = x;
        NodeHeaps.upHeap(heap, size - 1);
    }
    @Override
    public Node dequeue() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        Node result = heap[0];
        heap[0] = heap[--size];
        heap[size] = null;
        if (size != 0) {
            NodeHeaps.downHeap(heap, size, 0);
        }

        result.setHeapIndex(-1);
        return result;
    }
    @Override
    public Node first() {
        if (size == 0) {
            throw new NoSuchElementException();
        }

        return heap[0];
    }
    @Override
    public void changed() {
        NodeHeaps.downHeap(heap, size, 0);
    }

    /**
     * Updates the given node, using the index reported by {@link Node#getHeapIndex()}. If this index is negative, or
     * greater than the size of the heap, the node is assumed to not be a part of this queue and an
     * {@link IllegalArgumentException} is thrown. Otherwise, it may be repositioned such that the heap invariant is
     * maintained, if it is necessary to do so.
     * @param node the node that was changed
     */
    public void changed(Node node) {
        int pos = node.getHeapIndex();
        if (pos < 0 || pos > size) {
            throw new IllegalArgumentException("Node " + node + " does not belong to the queue");
        }

        int newPos = NodeHeaps.upHeap(heap, pos);
        NodeHeaps.downHeap(heap, size, newPos);
    }

    @Override
    public Comparator<? super Node> comparator() {
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        for(int i = 0; i < size; i++) {
            heap[i].setHeapIndex(-1);
            heap[i] = null;
        }
        size = 0;
    }

    /**
     * Trims the internal array to size.
     */
    public void trim() {
        heap = ObjectArrays.trim(heap, size);
    }
}
