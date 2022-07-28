package com.github.phantazmnetwork.core.inventory;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Implementation of an {@link InventoryObject} which uses caching for {@link ItemStack}s and recomputes
 * their representation every single time they are marked as dirty.
 */
public abstract class CachedInventoryObject implements InventoryObject {

    private ItemStack cache = null;

    private boolean dirty = true;

    @Override
    public @NotNull ItemStack getItemStack() {
        if (!shouldRedraw()) {
            return cache;
        }

        refreshCache();
        return cache;
    }

    @Override
    public boolean shouldRedraw() {
        return dirty;
    }

    /**
     * Gets the current cached {@link ItemStack}.
     *
     * @return The current cached {@link ItemStack}
     */
    protected @NotNull ItemStack peekStack() {
        if (cache == null) {
            refreshCache();
        }

        return cache;
    }

    /**
     * Sets the dirty flag. The next call to {@link #getItemStack()} will invoke {@link #computeStack()}.
     */
    protected void setDirty() {
        dirty = true;
    }

    private void refreshCache() {
        cache = Objects.requireNonNull(computeStack(), "Computed stack is null");
        dirty = false;
    }

    /**
     * Computes this inventory object's current representation as an {@link ItemStack}.
     *
     * @return This inventory object's current representation as an {@link ItemStack}
     */
    protected abstract @NotNull ItemStack computeStack();

}
