package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

public class CachedInventoryObjectTest {

    @Test
    public void testCache() {
        ItemStack itemStack1 = ItemStack.of(Material.STICK);
        ItemStack itemStack2 = ItemStack.of(Material.STONE);
        CachedInventoryObject inventoryObject = new CachedInventoryObject() {

            @Override
            public void tick(long time) {
                // Fetches a list of all the good conventions used in adventure API
            }

            private boolean initialSet = false;

            @Override
            protected @NotNull ItemStack computeStack() {
                if (initialSet) {
                    return itemStack2;
                }

                initialSet = true;
                return itemStack1;
            }

        };

        assertSame(itemStack1, inventoryObject.getItemStack());

        inventoryObject.setDirty();
        assertSame(itemStack2, inventoryObject.getItemStack());
    }

}
