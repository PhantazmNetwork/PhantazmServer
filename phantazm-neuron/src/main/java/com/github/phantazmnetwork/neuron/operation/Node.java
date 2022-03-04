package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Node implements Comparable<Node>, Iterable<Node>, Vec3I {
    private static final int PRIME = 31;

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

    private final int x;
    private final int y;
    private final int z;

    private float g;
    private float h;
    private int heapIndex;

    private Node parent;

    public Node(int x, int y, int z, float g, float h, @Nullable Node parent) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.g = g;
        this.h = h;
        this.heapIndex = -1;
        this.parent = parent;
    }

    public Node(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Node node) {
            return node.x == x && node.y == y && node.z == z;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = PRIME + x;
        result = PRIME * result + y;
        return PRIME * result + z;
    }

    @Override
    public int compareTo(@NotNull Node o) {
        return Float.compare(getF(), o.getF());
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getZ() {
        return z;
    }

    public float getG() {
        return g;
    }

    public float getH() {
        return h;
    }

    public float getF() {
        return g + h;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setH(float h) {
        this.h = h;
    }

    public boolean isOnHeap() {
        return heapIndex > -1;
    }

    public void setHeapIndex(int heapIndex) {
        this.heapIndex = heapIndex;
    }

    public int getHeapIndex() {
        return heapIndex;
    }

    public @Nullable Node getParent() {
        return parent;
    }

    public void setParent(@Nullable Node parent) {
        this.parent = parent;
    }

    public float distanceSquared(@NotNull Node other) {
        int x = this.x - other.x;
        int y = this.y - other.y;
        int z = this.z - other.z;
        return x * x + y * y + z * z;
    }

    public @NotNull Node invert() {
        Node prev = null;
        for(Node node : this) {
            node.setParent(prev);
            prev = node;
        }

        return prev == null ? this : prev;
    }

    @Override
    public @NotNull Iterator<Node> iterator() {
        return new NodeIterator();
    }

    @Override
    public String toString() {
        return "Node{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}