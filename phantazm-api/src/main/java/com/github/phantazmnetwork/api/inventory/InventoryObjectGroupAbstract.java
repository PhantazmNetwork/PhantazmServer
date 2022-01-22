package com.github.phantazmnetwork.api.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;
import java.util.function.Function;

/**
 * Abstract implementation of an {@link InventoryObjectGroup}.
 */
public abstract class InventoryObjectGroupAbstract implements InventoryObjectGroup {

    private final IntSet slots;

    private final IntSet unmodifiableSlots;

    /**
     * Creates an {@link InventoryObjectGroupAbstract}.
     * @param slots The slots to use for the group
     * @param unmodifiableMapper A mapper to make the provided slots as an unmodifiable view. The return type of the mapper will be the same as the object returned in {@link #getSlots()}
     */
    public InventoryObjectGroupAbstract(@NotNull IntSet slots,
                                        @NotNull Function<? super IntSet, ? extends @UnmodifiableView IntSet> unmodifiableMapper) {
        this.slots = Objects.requireNonNull(slots, "slots");
        this.unmodifiableSlots = unmodifiableMapper.apply(slots);
    }

    /**
     * Creates an {@link InventoryObjectGroupAbstract} which uses an unmodifiable view of an {@link IntSet}.
     * @param slots The slots to use for the group
     */
    public InventoryObjectGroupAbstract(@NotNull IntSet slots) {
        this(slots, IntSets::unmodifiable);
    }

    @Override
    public void addSlot(int slot) {
        if (!slots.add(slot)) {
            throw new IllegalArgumentException("Slot already added");
        }
    }

    @Override
    public void removeSlot(int slot) {
        if (!slots.remove(slot)) {
            throw new IllegalArgumentException("Slot not yet added");
        }
    }

    @Override
    public @NotNull @UnmodifiableView IntSet getSlots() {
        return unmodifiableSlots;
    }

    @Override
    public boolean isFull(@NotNull InventoryProfile profile) {
        for (int slot : slots) {
            if (profile.getInventoryObject(slot) == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty(@NotNull InventoryProfile profile) {
        for (int slot : slots) {
            if (profile.getInventoryObject(slot) != null) {
                return false;
            }
        }

        return true;
    }

}
