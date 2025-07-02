package org.phantazm.core.inventory;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BasicInventoryObjectGroup extends InventoryObjectGroupAbstract {
    private final InventoryObject defaultObject;

    public BasicInventoryObjectGroup(@NotNull InventoryProfile profile, @NotNull IntSet slots,
        @Nullable InventoryObject defaultObject) {
        super(profile, slots);
        this.defaultObject = defaultObject;
    }

    public BasicInventoryObjectGroup(@NotNull InventoryProfile profile, @NotNull IntSet slots) {
        this(profile, slots, null);
    }

    @Override
    public int pushInventoryObject(@NotNull InventoryObject toPush) {
        Objects.requireNonNull(toPush);

        InventoryProfile profile = getProfile();
        IntIterator intIterator = getSlots().intIterator();

        while (intIterator.hasNext()) {
            int slot = intIterator.nextInt();
            if (profile.compareAndSet(slot, defaultObject, toPush)) return slot;
        }

        throw new IllegalStateException("All slots are full");
    }

    @Override
    public @NotNull InventoryObject popInventoryObject() {
        InventoryProfile profile = getProfile();

        for (int i = getSlots().size(); i >= 0; i--) {
            InventoryObject popped = profile.getAndUpdate(i, existing -> {
                if (existing == null || existing.equals(defaultObject)) return existing;
                else return null;
            });

            if (popped != null && !popped.equals(defaultObject)) return popped;
        }

        throw new IllegalStateException("All slots are empty");
    }

    @Override
    public @Nullable InventoryObject defaultObject() {
        return defaultObject;
    }
}
