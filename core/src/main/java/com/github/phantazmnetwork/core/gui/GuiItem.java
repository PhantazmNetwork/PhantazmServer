package com.github.phantazmnetwork.core.gui;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface GuiItem {
    enum ClickType {
        LEFT_CLICK,
        RIGHT_CLICK,
        SHIFT_CLICK,
        MIDDLE_CLICK,
        DOUBLE_CLICK
    }

    /**
     * Handles a click, performing any required actions.
     *
     * @param owner     the GUI responsible for calling this GuiItem
     * @param slot      the slot this GuiItem is present in
     * @param clickType the type of click that occurs
     * @return true if the interaction should not be cancelled (will let players pick up items); false otherwise
     */
    boolean handleClick(@NotNull Gui owner, int slot, @NotNull ClickType clickType);

    /**
     * Called when this GuiItem is removed from a GUI.
     *
     * @param owner the owner
     * @param slot  the slot
     */
    void onRemove(@NotNull Gui owner, int slot);

    void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot);

    @NotNull ItemStack getStack();
}
