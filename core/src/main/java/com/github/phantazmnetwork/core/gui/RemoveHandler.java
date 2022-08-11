package com.github.phantazmnetwork.core.gui;

import org.jetbrains.annotations.NotNull;

/**
 * Handles the removal of a {@link GuiItem} from a particular slot.
 */
@FunctionalInterface
public interface RemoveHandler {
    /**
     * Called when a {@link GuiItem} is removed from a GUI without being replaced by another GuiItem.
     *
     * @param owner the owner
     * @param slot  the slot
     */
    void onRemove(@NotNull Gui owner, int slot);
}
