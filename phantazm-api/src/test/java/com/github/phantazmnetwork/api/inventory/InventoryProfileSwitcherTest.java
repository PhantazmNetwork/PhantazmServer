package com.github.phantazmnetwork.api.inventory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class InventoryProfileSwitcherTest {

    private final UUID firstUUID = UUID.fromString("41d2c3c1-8003-48e5-86da-ff8899a58dcd");

    private final UUID secondUUID = UUID.fromString("6458e77a-f565-4374-9de7-c2a20be572f3");

    @Test
    public void testDuplicateRegistration() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile();

        inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile);
        });
    }

    @Test
    public void testSwitchInitialProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile();
        inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstUUID);

        Assertions.assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
        Assertions.assertTrue(inventoryProfile.isVisible());
    }

    @Test
    public void testSwitchNewProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile firstInventoryProfile = new BasicInventoryProfile();
        InventoryProfile secondInventoryProfile = new BasicInventoryProfile();
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
        InventoryProfile inventoryProfile = new BasicInventoryProfile();
        inventoryProfileSwitcher.registerProfile(firstUUID, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstUUID);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
           inventoryProfileSwitcher.switchProfile(secondUUID);
        });
        Assertions.assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
        Assertions.assertTrue(inventoryProfile.isVisible());
    }

}
