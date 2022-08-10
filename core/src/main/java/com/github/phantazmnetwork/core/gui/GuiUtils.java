package com.github.phantazmnetwork.core.gui;

import net.minestom.server.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

/**
 * Utilities for working with GUIs.
 */
public final class GuiUtils {
    private GuiUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the estimated width for the given {@link InventoryType}. Used for formatting pages.
     *
     * @param type the type of inventory
     * @return the width of that inventory
     */
    public static int getEstimatedWidth(@NotNull InventoryType type) {
        return switch (type) {
            case CHEST_1_ROW, CHEST_2_ROW, CHEST_3_ROW, CHEST_4_ROW, CHEST_5_ROW, CHEST_6_ROW, SHULKER_BOX -> 9;
            case WINDOW_3X3, CRAFTING, GRINDSTONE, MERCHANT, SMITHING -> 3;
            case ANVIL, BREWING_STAND, BLAST_FURNACE, ENCHANTMENT, FURNACE, SMOKER, CARTOGRAPHY, STONE_CUTTER -> 2;
            case BEACON, LECTERN -> 1;
            case HOPPER -> 5;
            case LOOM -> 4;
        };
    }
}
