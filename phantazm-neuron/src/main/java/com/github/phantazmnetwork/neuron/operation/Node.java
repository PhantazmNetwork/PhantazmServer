package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Node implements Comparable<Node> {
    private final int x;
    private final int y;
    private final int z;

    private double g;
    private double h;

    private final Node parent;

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
        return Double.compare(g + h, o.g + o.h);
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

    public void setG(double g) {
        this.g = g;
    }

    public void setH(double h) {
        this.h = h;
    }

    public @Nullable Node getParent() {
        return parent;
    }
}