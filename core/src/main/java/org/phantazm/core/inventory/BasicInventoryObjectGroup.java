package org.phantazm.core.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public void pushInventoryObject(@NotNull InventoryObject toPush) {
        InventoryProfile profile = getProfile();
        for (int slot : getSlots()) {
            if (defaultObject != null) {
                if (!profile.hasInventoryObject(slot) || profile.getInventoryObject(slot).equals(defaultObject)) {
                    profile.setInventoryObject(slot, toPush);
                    return;
                }
            }
            else if (!profile.hasInventoryObject(slot)) {
                profile.setInventoryObject(slot, toPush);
                return;
            }
        }

        throw new IllegalStateException("All slots are full");
    }

    @Override
    public @NotNull InventoryObject popInventoryObject() {
        InventoryProfile profile = getProfile();
        for (int i = getSlots().size(); i >= 0; i--) {
            if (profile.hasInventoryObject(i)) {
                InventoryObject object = profile.getInventoryObject(i);

                if (!object.equals(defaultObject)) {
                    profile.removeInventoryObject(i);

                    if (defaultObject != null) {
                        profile.setInventoryObject(i, defaultObject);
                    }

                    return object;
                }
            }
        }

        throw new IllegalStateException("All slots are empty");
    }

    @Override
    public @Nullable InventoryObject defaultObject() {
        return defaultObject;
    }
}
