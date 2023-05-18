package org.phantazm.core.gui;

import org.jetbrains.annotations.NotNull;

/**
 * Handles the replacement of a {@link GuiItem} happening in a particular slot.
 */
@FunctionalInterface
public interface ReplaceHandler {
    /**
     * Called when the current {@link GuiItem} is replaced by another.
     *
     * @param owner   the GUI the replacement occurred in
     * @param newItem the item replacing this one
     * @param slot    the slot the replacement occurred in
     */
    void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot);
}
