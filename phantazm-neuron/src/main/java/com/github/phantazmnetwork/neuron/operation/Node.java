package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Node implements Comparable<Node>, Iterable<Node>, Vec3I {
    private class NodeIterator implements Iterator<Node> {
        private Node current = Node.this;

        @Override
        public boolean hasNext() {
            return current != null;
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

    private Node parent;

    public Node(int x, int y, int z, float g, float h, Node parent) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.g = g;
        this.h = h;
        this.parent = parent;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Node node) {
            return node.x == x && node.y == y && node.z == z && node.g == g && node.h == h;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 31 + x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = 31 * result + Float.hashCode(g);
        return 31 * result + Float.hashCode(h);
    }

    public boolean positionEquals(@Nullable Node other) {
        if(other != null) {
            return other.x == x && other.y == y && other.z == z;
        }

        return false;
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

    public @Nullable Node getParent() {
        return parent;
    }

    public void setParent(@Nullable Node parent) {
        this.parent = parent;
    }

    @Override
    public @NotNull Iterator<Node> iterator() {
        return new NodeIterator();
    }
}