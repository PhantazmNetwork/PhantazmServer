package com.github.phantazmnetwork.core.gui;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Periodically called in order to update the appearance of an {@link ItemStack}.
 */
@FunctionalInterface
public interface ItemUpdater {
    /**
     * Updates the given {@link ItemStack}. The returned value represents the new value.
     *
     * @param time    the current time
     * @param current the old item stack
     * @return the new item stack
     */
    @NotNull ItemStack update(long time, @NotNull ItemStack current);
}
