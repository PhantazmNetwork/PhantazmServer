package com.github.phantazmnetwork.core.gui;

import net.minestom.server.Tickable;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * An interaction-capable item in a GUI. When present in a dynamic GUI, may be animated (its tick method will be called
 * to update its {@link ItemStack} if necessary, which will then be retrieved through getStack).
 */
public interface GuiItem extends Tickable {
    enum ClickType {
        /**
         * Represents a left click.
         */
        LEFT_CLICK,

        /**
         * Represents a right click.
         */
        RIGHT_CLICK,

        /**
         * Represents a shift click.
         */
        SHIFT_CLICK,

        /**
         * Represents a middle click.
         */
        MIDDLE_CLICK,

        /**
         * Represents a double click.
         */
        DOUBLE_CLICK
    }

    /**
     * Handles a click, performing any required actions.
     *
     * @param owner     the GUI responsible for calling this GuiItem
     * @param slot      the slot this GuiItem is present in
     * @param clickType the type of click that occurs
     */
    void handleClick(@NotNull Gui owner, int slot, @NotNull ClickType clickType);

    /**
     * Called when this GuiItem is removed from a GUI without being replaced by another GuiItem.
     *
     * @param owner the owner
     * @param slot  the slot
     */
    void onRemove(@NotNull Gui owner, int slot);

    /**
     * Called when this GuiItem is replaced by another.
     *
     * @param owner   the GUI the replacement occurred in
     * @param newItem the item replacing this one
     * @param slot    the slot the replacement occurred in
     */
    void onReplace(@NotNull Gui owner, @NotNull GuiItem newItem, int slot);

    /**
     * Gets the stack which should be displayed by this GuiItem.
     *
     * @return the {@link ItemStack} this GuiItem should display
     */
    @NotNull ItemStack getStack();
}
