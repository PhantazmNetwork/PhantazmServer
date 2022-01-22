package com.github.phantazmnetwork.api.inventory;

import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.function.Supplier;

public class InventoryProfileTest {

    @Test
    public void testSetObjectInOccupiedSlot() {
        AbstractInventory inventory = Mockito.mock(Inventory.class);
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(() -> Optional.of(inventory), itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                inventoryProfile.setInventoryObject(slot, inventoryObject));
    }

    @Test
    public void testSetObjectInUnoccupiedSlot() {
        AbstractInventory inventory = Mockito.mock(Inventory.class);
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(() -> Optional.of(inventory), itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        Assertions.assertSame(inventoryObject, inventoryProfile.getInventoryObject(slot));
    }

    @Test
    public void testRemoveObjectInOccupiedSlot() {
        AbstractInventory inventory = Mockito.mock(Inventory.class);
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(() -> Optional.of(inventory), itemStack);

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

    @Test
    public void testSetVisible() {
        int slotCount = 9;
        InventoryProfile profile = new BasicInventoryProfile(slotCount);
        AbstractInventory inventory = Mockito.mock(Inventory.class);
        ItemStack stack = ItemStack.of(Material.STICK);

        Supplier<Optional<AbstractInventory>> inventorySupplier = () -> Optional.of(inventory);
        for (int i = 0; i < slotCount; i++) {
            profile.setInventoryObject(i, new BasicInventoryObject(inventorySupplier, stack));
        }
        profile.setVisible(true);

        for (int i = 0; i < slotCount; i++) {
            Mockito.verify(inventory, Mockito.times(1)).setItemStack(i, stack);
        }
    }

    @Test
    public void testSetInvisible() {
        int slotCount = 9;
        InventoryProfile profile = new BasicInventoryProfile(slotCount);
        AbstractInventory inventory = Mockito.mock(Inventory.class);
        ItemStack stack = ItemStack.of(Material.STICK);

        Supplier<Optional<AbstractInventory>> inventorySupplier = () -> Optional.of(inventory);
        for (int i = 0; i < slotCount; i++) {
            profile.setInventoryObject(i, new BasicInventoryObject(inventorySupplier, stack));
        }
        profile.setVisible(true);
        profile.setVisible(false);

        for (int i = 0; i < slotCount; i++) {
            Mockito.verify(inventory, Mockito.times(1)).setItemStack(i, stack);
        }
    }

}
