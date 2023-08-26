package org.phantazm.core.inventory;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.phantazm.commons.Namespaces;
import org.phantazm.core.player.PlayerView;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class InventoryAccessRegistryTest {

    private final Key firstKey = Key.key(Namespaces.PHANTAZM, "ade229bf");

    private final Key secondKey = Key.key(Namespaces.PHANTAZM, "s1lly_w33d_m4n");

    private final PlayerView mockPlayer = Mockito.mock(PlayerView.class);

    public InventoryAccessRegistryTest() {
        Mockito.when(mockPlayer.getPlayer()).thenReturn(Optional.empty());
    }

    private static @NotNull InventoryAccess newAccess(@NotNull InventoryProfile profile) {
        return new InventoryAccess(profile, Collections.emptyMap());
    }

    @Test
    public void testDuplicateRegistration() {
        InventoryAccessRegistry inventoryAccessRegistry =
            new BasicInventoryAccessRegistry(Mockito.mock(PlayerView.class));
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);

        inventoryAccessRegistry.registerAccess(firstKey, newAccess(inventoryProfile));

        assertThrowsExactly(IllegalArgumentException.class,
            () -> inventoryAccessRegistry.registerAccess(firstKey, newAccess(inventoryProfile)));
    }

    @Test
    public void testSwitchInitialProfile() {
        InventoryAccessRegistry inventoryAccessRegistry =
            new BasicInventoryAccessRegistry(Mockito.mock(PlayerView.class));
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryAccessRegistry.registerAccess(firstKey, newAccess(inventoryProfile));

        inventoryAccessRegistry.switchAccess(firstKey);

        assertSame(inventoryProfile, inventoryAccessRegistry.getCurrentAccess().orElseThrow().profile());
    }

    @Test
    public void testSwitchNewProfile() {
        InventoryAccessRegistry inventoryAccessRegistry =
            new BasicInventoryAccessRegistry(Mockito.mock(PlayerView.class));
        InventoryProfile firstInventoryProfile = new BasicInventoryProfile(1);
        InventoryProfile secondInventoryProfile = new BasicInventoryProfile(1);
        inventoryAccessRegistry.registerAccess(firstKey, newAccess(firstInventoryProfile));
        inventoryAccessRegistry.registerAccess(secondKey, newAccess(secondInventoryProfile));

        inventoryAccessRegistry.switchAccess(firstKey);
        inventoryAccessRegistry.switchAccess(secondKey);

        assertSame(secondInventoryProfile, inventoryAccessRegistry.getCurrentAccess().orElseThrow().profile());
    }

    @Test
    public void testSwitchUnregisteredProfile() {
        InventoryAccessRegistry inventoryAccessRegistry =
            new BasicInventoryAccessRegistry(Mockito.mock(PlayerView.class));
        InventoryProfile inventoryProfile = new BasicInventoryProfile(1);
        inventoryAccessRegistry.registerAccess(firstKey, newAccess(inventoryProfile));

        inventoryAccessRegistry.switchAccess(firstKey);

        assertThrowsExactly(IllegalArgumentException.class, () -> inventoryAccessRegistry.switchAccess(secondKey));
        assertSame(inventoryProfile, inventoryAccessRegistry.getCurrentAccess().orElseThrow().profile());
    }

}
