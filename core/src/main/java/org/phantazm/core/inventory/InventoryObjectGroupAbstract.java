package org.phantazm.core.inventory;

import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
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
    private final InventoryProfile profile;
    private final IntSet slots;
    private final IntSet unmodifiableSlots;

    /**
     * Creates an {@link InventoryObjectGroupAbstract}.
     *
     * @param profile            The profile to which this group belongs
     * @param slots              The slots to use for the group
     * @param unmodifiableMapper A mapper to make the provided slots as an unmodifiable view. The return type of the
     *                           mapper will be the same as the object returned in {@link #getSlots()}
     */
    public InventoryObjectGroupAbstract(@NotNull InventoryProfile profile, @NotNull IntSet slots,
        @NotNull Function<? super IntSet, ? extends IntSet> unmodifiableMapper) {
        this.profile = Objects.requireNonNull(profile);
        this.slots = new IntRBTreeSet(slots);
        this.unmodifiableSlots = Objects.requireNonNull(unmodifiableMapper.apply(this.slots), "mapped slots");
    }

    /**
     * Creates an {@link InventoryObjectGroupAbstract} which uses an unmodifiable view of an {@link IntSet}.
     *
     * @param profile The profile to use for the group
     * @param slots   The slots to use for the group
     */
    public InventoryObjectGroupAbstract(@NotNull InventoryProfile profile, @NotNull IntSet slots) {
        this(profile, slots, IntSets::unmodifiable);
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
    public @NotNull
    @UnmodifiableView IntSet getSlots() {
        return unmodifiableSlots;
    }

    @Override
    public boolean isFull() {
        for (int slot : slots) {
            if (!profile.hasInventoryObject(slot)) {
                return false;
            }

            InventoryObject object = profile.getInventoryObject(slot);
            if (object.equals(defaultObject())) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isEmpty() {
        for (int slot : slots) {
            if (profile.hasInventoryObject(slot)) {
                InventoryObject object = profile.getInventoryObject(slot);

                InventoryObject defaultObject = defaultObject();
                if (!object.equals(defaultObject)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public @NotNull InventoryProfile getProfile() {
        return profile;
    }
}
