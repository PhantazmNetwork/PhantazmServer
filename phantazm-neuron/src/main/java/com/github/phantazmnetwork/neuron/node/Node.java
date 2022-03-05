package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.ImmutableVec3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <p>Represents a particular point along a path. Each node may be connected to at most one other node (referred to as
 * its "parent"), meaning that it may be treated as a singly-linked list.</p>
 *
 * <p>This class implements {@link Iterable}. Its iterator will return each node in order, starting with the node being
 * iterated, and ending with the first node in the chair to have a null parent. This means that every node's iterator is
 * guaranteed to iterate <i>at least</i> once.</p>
 *
 * <p>Since nodes can be linked in arbitrary ways, it is possible to have a structure in which a node's parent is
 * <i>itself</i>, or a node earlier in the chain. If this is the case, the iterator for any of the nodes present in the
 * structure will never terminate naturally. When creating chains of nodes, implementations must take care to ensure
 * such situations never occur, as keeping a record of visited nodes would add an unacceptable amount of overhead to
 * node iteration.</p>
 *
 * <p>Node extends {@link ImmutableVec3I}. It also implements {@link Comparable}, with a natural ordering that is
 * consistent with equals. Nodes are compared first by x value, then y, then z. This allows nodes to be used as a key in
 * comparison-based maps, and doing so is "safe" in that nodes are only compared by their immutable attributes.</p>
 *
 * <p>Although the position of the node cannot change throughout its lifespan, its parent, along with {@code g} and
 * {@code h} values, can and indeed are expected to be changed many times throughout path calculation.</p>
 *
 * <p>Node objects are designed to work efficiently with NodeQueue. To this end, each node stores an index —
 * called {@code heapIndex} — which corresponds to the node's position within the queue's backing array. This index
 * will be negative if the node is not currently in a queue. It will be set to an appropriate value by the queue when it
 * is added and set to -1 when it is no longer present in the queue.</p>
 *
 * <p>Although this form of index caching is very convenient — after all it gives us O(1) {@code contains} — it can
 * lead to problems if an attempt is made to store a node in more than one queue at a time. This is because when the
 * node is added to a second queue without being removed from the first, its index will be updated to reflect its
 * location within the second queue. However, this may result in the node's index becoming invalid for the <i>first</i>
 * queue. If an attempt is made to update the node's position in the first queue, an exception may be thrown, or the
 * queue may attempt to update a different node. Therefore, care must be taken to ensure that nodes are <i>never</i>
 * present in two queues at any given time.</p>
 *
 * @see NodeQueue
 * @see Vec3I
 */
public class Node extends ImmutableVec3I implements Comparable<Node>, Iterable<Node> {
    private class NodeIterator implements Iterator<Node> {
        private Node current = Node.this;

        @Override
        public boolean hasNext() {
            return this.current != null;
        }

        @Override
        public Node next() {
            Node current = this.current;
            if(current == null) {
                throw new NoSuchElementException();
            }

            this.current = current.parent;
            return current;
        }
    }

    private float g;
    private float h;
    private int heapIndex;

    private Node parent;

    public Node(int x, int y, int z, float g, float h, @Nullable Node parent) {
        super(x, y, z);
        this.g = g;
        this.h = h;
        this.heapIndex = -1;
        this.parent = parent;
    }

    /**
     * Returns the {@code g}-score for this node.
     * @return this node's {@code g}-score
     */
    public float getG() {
        return g;
    }

    /**
     * Returns the {@code h}-score for this node.
     * @return this node's {@code h}-score
     */
    public float getH() {
        return h;
    }

    /**
     * Returns the {@code f}-score for this node, defined as the sum of its {@code g} and {@code h} scores.
     * @return this node's {@code f}-score
     */
    public float getF() {
        return g + h;
    }

    /**
     * Sets the {@code g}-score for this node.
     * @param g the new g-score
     */
    public void setG(float g) {
        this.g = g;
    }

    /**
     * Sets the {@code h}-score for this node.
     * @param h the new h-score
     */
    public void setH(float h) {
        this.h = h;
    }

    /**
     * Determines if this node is currently present inside a {@link NodeQueue} by checking its heap index.
     * @return true if this node is present in a NodeQueue, false otherwise
     */
    public boolean isOnHeap() {
        return heapIndex > -1;
    }

    /**
     * Sets the heap index. This is only used internally by {@link NodeQueue}.
     * @param heapIndex the new heap index, or -1 to indicate removal
     */
    void setHeapIndex(int heapIndex) {
        this.heapIndex = heapIndex;
    }

    /**
     * Gets the current heap index.
     * @return the current heap index, or -1 if this node is not part of any {@link NodeQueue}
     */
    public int getHeapIndex() {
        return heapIndex;
    }

    /**
     * Gets the parent node. May be null if this node has no parent.
     * @return the parent node
     */
    public @Nullable Node getParent() {
        return parent;
    }

    /**
     * Sets the parent of this node, which may be null.
     * @param parent the new parent
     */
    public void setParent(@Nullable Node parent) {
        this.parent = parent;
    }

    /**
     * <p>Reverses the ordering of the linked list represented by this node. That is, given the following structure of
     * different nodes labelled a, b, and c, with arrows pointing to the parent of the node on the left:</p>
     *
     * <code>
     *     a -> b -> c
     * </code>
     *
     * <p>Calling reverse will produce the following list:</p>
     *
     * <code>
     *     c -> b -> a
     * </code>
     *
     * <p>The returned node will be the last node in the list before it was inverted. In the example shown above, this
     * is {@code c}.</p>
     *
     * <p>Special case: when this node has no parent, this node is returned.</p>
     * @return the starting node of the reversed linked list, which was the last node in the list before reversal
     */
    public @NotNull Node reverse() {
        Node prev = null;
        for(Node node : this) {
            node.setParent(prev);
            prev = node;
        }

        return prev == null ? this : prev;
    }

    @Override
    public int compareTo(@NotNull Node o) {
        int xCompare = Integer.compare(x, o.x);
        if(xCompare == 0) {
            int yCompare = Integer.compare(y, o.y);
            if(yCompare == 0) {
                return Integer.compare(z, o.z);
            }

            return yCompare;
        }

        return xCompare;
    }

    @Override
    public String toString() {
        return "Node{x=" + x + ", y=" + y + ", z=" + z + ", g=" + g + ", h=" + h + "}";
    }

    @Override
    public @NotNull Iterator<Node> iterator() {
        return new NodeIterator();
    }
}