package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class Node implements Comparable<Node>, Iterable<Node> {
    private class NodeIterator implements Iterator<Node> {
        private Node current = Node.this;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public Node next() {
            Node current = this.current;
            this.current = current.parent;
            return current;
        }
    }

    private final int x;
    private final int y;
    private final int z;

    private double g;
    private double h;

    private Node parent;

    public Node(int x, int y, int z, int g, int h, Node parent) {
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
        int result = 1;

        result = 31 * result + x;
        result = 31 * result + y;
        result = 31 * result + z;
        result = (int) (31 * result + g);
        result = (int) (31 * result + h);

        return result;
    }

    @Override
    public int compareTo(@NotNull Node o) {
        return Double.compare(getF(), o.getF());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public double getG() {
        return g;
    }

    public double getH() {
        return h;
    }

    public double getF() {
        return g + h;
    }

    public void setG(double g) {
        this.g = g;
    }

    public void setH(double h) {
        this.h = h;
    }

    public @Nullable Node getParent() {
        return parent;
    }

    public void setParent(@Nullable Node parent) {
        this.parent = parent;
    }

    @NotNull
    @Override
    public Iterator<Node> iterator() {
        return new NodeIterator();
    }
}