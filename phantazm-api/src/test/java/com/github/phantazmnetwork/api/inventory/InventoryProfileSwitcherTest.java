package com.github.phantazmnetwork.api.inventory;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class InventoryProfileSwitcherTest {

    private final Key firstKey = Key.key(Namespaces.PHANTAZM, "ade229bf");

    private final Key secondKey = Key.key(Namespaces.PHANTAZM, "s1lly_w33d_m4n");

    @Test
    public void testDuplicateRegistration() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);

        inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile);

        assertThrowsExactly(IllegalArgumentException.class,
                            () -> inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile)
        );
    }

    @Test
    public void testSwitchInitialProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstKey);

        assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
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

        assertSame(secondInventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
    }

    @Test
    public void testSwitchUnregisteredProfile() {
        InventoryProfileSwitcher inventoryProfileSwitcher = new BasicInventoryProfileSwitcher();
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryProfileSwitcher.registerProfile(firstKey, inventoryProfile);

        inventoryProfileSwitcher.switchProfile(firstKey);

        assertThrowsExactly(IllegalArgumentException.class, () -> inventoryProfileSwitcher.switchProfile(secondKey));
        assertSame(inventoryProfile, inventoryProfileSwitcher.getCurrentProfile());
    }

}
