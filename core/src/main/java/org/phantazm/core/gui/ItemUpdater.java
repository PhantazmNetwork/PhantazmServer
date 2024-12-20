package org.phantazm.core.gui;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Periodically called in order to update the appearance of an {@link ItemStack}.
 */
public interface ItemUpdater {
    /**
     * Updates the given {@link ItemStack}. The returned value represents the new value.
     *
     * @param gui     the gui updating this item
     * @param time    the current time
     * @param current the old item stack
     * @return the new item stack
     */
    @NotNull ItemStack update(@NotNull Gui gui, long time, @NotNull ItemStack current);

    /**
     * Determines if this ItemUpdater can update the item.
     *
     * @param gui     the gui checking to see if this item needs to be updated
     * @param time    the current time
     * @param current the current {@link ItemStack}
     * @return true if this ItemUpdater has an update, false otherwise
     */
    boolean hasUpdate(@NotNull Gui gui, long time, @NotNull ItemStack current);
}
