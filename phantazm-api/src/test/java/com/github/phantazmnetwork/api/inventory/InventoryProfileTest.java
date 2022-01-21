package com.github.phantazmnetwork.api.inventory;

import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.entity.Player;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class InventoryProfileTest {

    private final UUID uuid = UUID.fromString("41d2c3c1-8003-48e5-86da-ff8899a58dcd");

    private @NotNull PlayerView createPlayerView(@NotNull Player player) {
        return new PlayerView() {
            @Override
            public @NotNull UUID getUUID() {
                return uuid;
            }

            @Override
            public @NotNull Optional<Player> getPlayer() {
                return Optional.of(player);
            }
        };
    }

    @Test
    public void testSetObjectInOccupiedSlot() {
        InventoryProfile inventoryProfile = new BasicInventoryProfile();
        PlayerView playerView = createPlayerView(Mockito.mock(Player.class));
        ItemStack itemStack = ItemStack.of(Material.STICK);
        int slot = 1;
        InventoryObject inventoryObject = new BasicInventoryObject(playerView, itemStack, slot);

        inventoryProfile.setInventoryObject(slot, inventoryObject);

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
           inventoryProfile.setInventoryObject(slot, inventoryObject);
        });
    }

    @Test
    public void testRemoveObject() {
        InventoryProfile inventoryProfile = new BasicInventoryProfile();
        PlayerView playerView = createPlayerView(Mockito.mock(Player.class));
        ItemStack itemStack = ItemStack.of(Material.STICK);
        int slot = 1;
        InventoryObject inventoryObject = new BasicInventoryObject(playerView, itemStack, slot);
        AtomicBoolean isRemoved = new AtomicBoolean();
        inventoryObject.addRemovalHandler(() -> isRemoved.set(true));

        inventoryProfile.setInventoryObject(slot, inventoryObject);
        inventoryProfile.removeInventoryObject(slot);

        Assertions.assertTrue(isRemoved.get());
    }

    @Test
    public void testRemoveObjectInUnoccupiedSlot() {
        InventoryProfile inventoryProfile = new BasicInventoryProfile();

        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            inventoryProfile.removeInventoryObject(1);
        });
    }

}
