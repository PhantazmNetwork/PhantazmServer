package com.github.phantazmnetwork.api.inventory;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Objects;
import java.util.Optional;

public class InventoryObjectGroupTest {

    private @NotNull InventoryObjectGroup createGroup(@NotNull IntSet slots) {
        Objects.requireNonNull(slots, "slots");

        return new InventoryObjectGroupAbstract(slots) {
            @Override
            public int computeNextPushSlot(@NotNull InventoryProfile profile) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int computeNextPopSlot(@NotNull InventoryProfile profile) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Test
    public void testEmptyWhenEmpty() {
        IntSet slots = IntSet.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
        InventoryObjectGroup group = createGroup(slots);
        InventoryProfile profile = new BasicInventoryProfile(slots.size());

        Assertions.assertTrue(group.isEmpty(profile));
    }

    @Test
    public void testEmptyWhenNotEmpty() {
        IntSet slots = IntSet.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
        InventoryObjectGroup group = createGroup(slots);
        InventoryProfile profile = new BasicInventoryProfile(slots.size());
        AbstractInventory inventory = Mockito.mock(Inventory.class);
        ItemStack stack = ItemStack.of(Material.STICK);

        profile.setInventoryObject(0, new BasicInventoryObject(() -> Optional.of(inventory), stack));

        Assertions.assertFalse(group.isEmpty(profile));
    }

}
