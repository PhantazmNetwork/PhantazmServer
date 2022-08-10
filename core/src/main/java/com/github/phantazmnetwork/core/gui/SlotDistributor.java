package com.github.phantazmnetwork.core.gui;

import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SlotDistributor {
    int[] distribute(int width, int height, int itemCount);
}
