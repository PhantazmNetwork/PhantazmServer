package org.phantazm.core.inventory;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InventoryProfileTest {
    @Test
    public void testSetObjectInUnoccupiedSlot() {
        int slot = 0;
        InventoryProfile inventoryProfile = new BasicInventoryProfile(slot + 1);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        InventoryObject inventoryObject = new BasicInventoryObject(itemStack);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        assertSame(inventoryObject, inventoryProfile.getInventoryObject(slot));
    }
}
