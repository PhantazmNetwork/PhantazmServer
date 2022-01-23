package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class InventoryProfileTest {

    @Test
    public void testSetObjectInOccupiedSlot() {
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                inventoryProfile.setInventoryObject(slot, inventoryObject));
    }

    @Test
    public void testSetObjectInUnoccupiedSlot() {
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        Assertions.assertSame(inventoryObject, inventoryProfile.getInventoryObject(slot));
    }

    @Test
    public void testRemoveObjectInOccupiedSlot() {
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);
        inventoryProfile.removeInventoryObject(slot);

        Assertions.assertFalse(inventoryProfile.hasInventoryObject(slot));
    }

    @Test
    public void testRemoveObjectInUnoccupiedSlot() {
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                inventoryProfile.removeInventoryObject(0));
    }

}
