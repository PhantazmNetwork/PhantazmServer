package com.github.phantazmnetwork.api.inventory;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InventoryProfileSwitcherTest {

    private final Key firstKey = Key.key("phantazm", "ade229bf-d062-46e8-99d8-97b667d5a127");

    private final Key secondKey = Key.key("phantazm", "31ee3877-dbd8-423a-95e4-9181b8acfe74");

    @Test
    public void testDuplicateRegistration() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);

        inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile));
    }

    @Test
    public void testSwitchInitialProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstKey);

        Assertions.assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
    }

    @Test
    public void testSwitchNewProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile firstInventoryProfile = new BasicInventoryProfile(1);
        InventoryProfile secondInventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstKey, firstInventoryProfile);
        inventoryProfileSwitcher.registerProfile(secondKey, secondInventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstKey);
        inventoryProfileSwitcher.switchProfile(secondKey);

        Assertions.assertSame(secondInventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
    }

    @Test
    public void testSwitchUnregisteredProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstKey);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                inventoryProfileSwitcher.switchProfile(secondKey));
        Assertions.assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
    }

}
