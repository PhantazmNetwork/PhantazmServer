package com.github.phantazmnetwork.core.gui;

@FunctionalInterface
public interface SlotDistributor {
    int[] distribute(int width, int height, int itemCount);
}
