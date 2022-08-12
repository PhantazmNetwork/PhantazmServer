package com.github.phantazmnetwork.core.inventory;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;


public class InventoryProfileTest {

    @Test
    public void testSetObjectInOccupiedSlot() {
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        assertThrowsExactly(IllegalArgumentException.class,
                () -> inventoryProfile.setInventoryObject(slot, inventoryObject));
    }

    @Test
    public void testSetObjectInUnoccupiedSlot() {
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        assertSame(inventoryObject, inventoryProfile.getInventoryObject(slot));
    }

    @Test
    public void testRemoveObjectInOccupiedSlot() {
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);
        inventoryProfile.removeInventoryObject(slot);

        assertFalse(inventoryProfile.hasInventoryObject(slot));
    }

    @Test
    public void testRemoveObjectInUnoccupiedSlot() {
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);

        assertThrowsExactly(IllegalArgumentException.class, () -> inventoryProfile.removeInventoryObject(0));
    }

}
