package com.github.phantazmnetwork.api.inventory;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InventoryProfileSwitcherTest {

    private final Key firstKey = Key.key("phantazm", "ade229bf");

    private final Key secondKey = Key.key("phantazm", "s1lly_w33d_m4n");

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
