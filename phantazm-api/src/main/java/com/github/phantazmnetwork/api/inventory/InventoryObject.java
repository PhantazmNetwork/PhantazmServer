package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represents an item in a {@link net.minestom.server.entity.Player}'s inventory.
 */
public interface InventoryObject {

    /**
     * Updates the {@link ItemStack}'s representation in the associated {@link net.minestom.server.entity.Player}'s
     * {@link net.minestom.server.inventory.PlayerInventory}.
     */
    void updateInInventory();

    /**
     * Gets the currently associated {@link ItemStack}.
     * @return The currently associated {@link ItemStack}
     */
    @NotNull ItemStack getItemStack();

    /**
     * Sets the {@link ItemStack} used by this inventory object.
     * @param itemStack The {@link ItemStack} to use
     */
    void setItemStack(@NotNull ItemStack itemStack);

    /**
     * Checks whether the inventory object is considered to currently be visible.
     * @return Whether the inventory object is considered to currently be visible
     */
    boolean isVisible();

    /**
     * Sets whether the inventory object should be considered visible.
     * @param visible Whether the inventory object should be considered visible
     */
    void setVisible(boolean visible);

    /**
     * Checks whether the inventory object is considered to currently be selected.
     * @return Whether the inventory object is considered to currently be selected
     */
    boolean isSelected();

    /**
     * Sets whether the inventory object should be considered selected.
     * @param selected Whether the inventory object should be considered selected
     */
    void setSelected(boolean selected);

    /**
     * Simulates a left click on the inventory object.
     */
    void onLeftClick();

    /**
     * Simulates a right click on the inventory object.
     */
    void onRightClick();

    /**
     * Simulates the inventory object's removal.
     */
    void onRemove();

    /**
     * Adds a handler to the inventory object for when it is left-clicked.
     * @param handler The handler to add
     */
    void addLeftClickHandler(@NotNull Runnable handler);

    /**
     * Adds a handler to the inventory object for when it is right-clicked.
     * @param handler The handler to add
     */
    void addRightClickHandler(@NotNull Runnable handler);

    /**
     * Adds a handler to the inventory object for when its visibility changes.
     * @param handler The handler to add
     */
    void addVisibilityChangedHandler(@NotNull Runnable handler);

    /**
     * Adds a handler to the inventory object for when it is selected or unselected.
     * @param handler The handler to add
     */
    void addSelectionChangedHandler(@NotNull Runnable handler);

    /**
     * Adds a handler to the inventory object for when it is removed.
     * @param handler The handler to add
     */
    void addRemovalHandler(@NotNull Runnable handler);

}
