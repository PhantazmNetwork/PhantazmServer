package com.github.phantazmnetwork.api.inventory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class InventoryProfileSwitcherTest {

    private final UUID firstUUID = UUID.fromString("ade229bf-d062-46e8-99d8-97b667d5a127");

    private final UUID secondUUID = UUID.fromString("31ee3877-dbd8-423a-95e4-9181b8acfe74");

    @Test
    public void testDuplicateRegistration() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);

        inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () ->
                inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile));
    }

    @Test
    public void testSwitchInitialProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstUUID);

        Assertions.assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
        Assertions.assertTrue(inventoryProfile.isVisible());
    }

    @Test
    public void testSwitchNewProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile firstInventoryProfile = new BasicInventoryProfile(1);
        InventoryProfile secondInventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstUUID, firstInventoryProfile);
        inventoryProfileSwitcher.registerProfile(secondUUID, secondInventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstUUID);
        inventoryProfileSwitcher.switchProfile(secondUUID);

        Assertions.assertSame(secondInventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
        Assertions.assertFalse(firstInventoryProfile.isVisible());
        Assertions.assertTrue(secondInventoryProfile.isVisible());
    }

    @Test
    public void testSwitchUnregisteredProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstUUID);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
           inventoryProfileSwitcher.switchProfile(secondUUID);
        });
        Assertions.assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
        Assertions.assertTrue(inventoryProfile.isVisible());
    }

}
