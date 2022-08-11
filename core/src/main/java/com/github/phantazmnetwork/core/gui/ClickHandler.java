package com.github.phantazmnetwork.core.gui;

import org.jetbrains.annotations.NotNull;

public interface ClickHandler {
    /**
     * Handles a click, performing any required actions.
     *
     * @param owner     the GUI responsible for calling this GuiItem
     * @param slot      the slot the GuiItem is present in
     * @param clickType the type of click that occurs
     */
    void handleClick(@NotNull Gui owner, int slot, @NotNull GuiItem.ClickType clickType);
}
