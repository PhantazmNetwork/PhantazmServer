package com.github.phantazmnetwork.api.inventory;

import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Basic implementation of an {@link InventoryObject}.
 */
public class BasicInventoryObject implements InventoryObject {

    private final Collection<Runnable> leftClickHandlers = new ArrayList<>();

    private final Collection<Runnable> rightClickHandlers = new ArrayList<>();

    private final Collection<Runnable> visibilityChangedHandlers = new ArrayList<>();

    private final Collection<Runnable> selectionChangedHandlers = new ArrayList<>();

    private final Collection<Runnable> removalHandlers = new ArrayList<>();

    private final PlayerView playerView;

    private final int slot;

    private ItemStack itemStack;

    private boolean visible;

    private boolean selected;

    /**
     * Creates a basic {@link InventoryObject}.
     * @param playerView A view used for an associated player
     * @param itemStack The initial {@link ItemStack} of the {@link InventoryObject}
     * @param slot The slot in which the {@link InventoryObject} stays in
     */
    public BasicInventoryObject(@NotNull PlayerView playerView, @NotNull ItemStack itemStack, int slot) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.itemStack = Objects.requireNonNull(itemStack, "itemStack");
        this.slot = slot;
    }

    @Override
    public void updateInInventory() {
        if (!visible) {
            throw new IllegalStateException("Can't update a InventoryObject that is not visible");
        }

        updateInInventoryInternal(itemStack);
    }

    private void updateInInventoryInternal(@NotNull ItemStack itemStack) {
        playerView.getPlayer().ifPresent(player -> player.getInventory().setItemStack(slot, itemStack));
    }

    @Override
    public @NotNull ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void setItemStack(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(boolean visible) {
        if (this.visible != visible) {
            this.visible = visible;

            if (visible) {
                updateInInventory();
            }
            else {
                updateInInventoryInternal(ItemStack.AIR);
            }

            for (Runnable handler : visibilityChangedHandlers) {
                handler.run();
            }
        }
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;

        for (Runnable handler : selectionChangedHandlers) {
            handler.run();
        }
    }

    @Override
    public void onLeftClick() {
        for (Runnable handler : leftClickHandlers) {
            handler.run();
        }
    }

    @Override
    public void onRightClick() {
        for (Runnable handler : rightClickHandlers) {
            handler.run();
        }
    }

    @Override
    public void onRemove() {
        setItemStack(ItemStack.AIR);
        if (visible) {
            updateInInventory();
        }

        for (Runnable handler : removalHandlers) {
            handler.run();
        }
    }

    @Override
    public void addLeftClickHandler(@NotNull Runnable handler) {
        leftClickHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void addRightClickHandler(@NotNull Runnable handler) {
        rightClickHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void addVisibilityChangedHandler(@NotNull Runnable handler) {
        visibilityChangedHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void addSelectionChangedHandler(@NotNull Runnable handler) {
        selectionChangedHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

    @Override
    public void addRemovalHandler(@NotNull Runnable handler) {
        removalHandlers.add(Objects.requireNonNull(handler, "handler"));
    }

}
