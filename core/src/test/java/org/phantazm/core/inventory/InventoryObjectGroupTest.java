package org.phantazm.core.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryObjectGroupTest {

    private @NotNull InventoryObjectGroup createGroup(@NotNull InventoryProfile profile, @NotNull IntSet slots) {
        Objects.requireNonNull(slots, "slots");

        return new InventoryObjectGroupAbstract(profile, slots) {

            @Override
            public void pushInventoryObject(@NotNull InventoryObject toPush) {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NotNull InventoryObject popInventoryObject() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @Nullable InventoryObject defaultObject() {
                return null;
            }

        };
    }

    @Test
    public void testEmptyWhenEmpty() {
        IntSet slots = IntSet.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
        InventoryProfile profile = new BasicInventoryProfile(slots.size());
        InventoryObjectGroup group = createGroup(profile, slots);

        assertTrue(group.isEmpty());
    }

    @Test
    public void testEmptyWhenNotEmpty() {
        IntSet slots = IntSet.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
        InventoryProfile profile = new BasicInventoryProfile(slots.size());
        InventoryObjectGroup group = createGroup(profile, slots);
        ItemStack stack = ItemStack.of(Material.STICK);

        profile.setInventoryObject(0, new BasicInventoryObject(stack));

        assertFalse(group.isEmpty());
    }

}
