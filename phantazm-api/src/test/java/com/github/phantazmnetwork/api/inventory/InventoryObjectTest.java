package com.github.phantazmnetwork.api.inventory;

import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.UUID;

public class InventoryObjectTest {

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
    public void testVisibilityUpdatesInventory() {
        Player player = Mockito.mock(Player.class);
        PlayerInventory playerInventory = Mockito.mock(PlayerInventory.class);
        Mockito.when(player.getInventory()).thenReturn(playerInventory);
        PlayerView playerView = createPlayerView(player);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        int slot = 1;
        InventoryObject inventoryObject = new BasicInventoryObject(playerView, itemStack, slot);

        inventoryObject.setVisible(true);
        Mockito.verify(playerInventory, Mockito.times(1)).setItemStack(slot, itemStack);
        Assertions.assertSame(itemStack, inventoryObject.getItemStack());
    }

    @Test
    public void testVisibleUpdateInInventory() {
        Player player = Mockito.mock(Player.class);
        PlayerInventory playerInventory = Mockito.mock(PlayerInventory.class);
        Mockito.when(player.getInventory()).thenReturn(playerInventory);
        PlayerView playerView = createPlayerView(player);
        ItemStack originalItemStack = ItemStack.of(Material.STICK);
        ItemStack newItemStack = ItemStack.of(Material.DIAMOND);
        int slot = 1;
        InventoryObject inventoryObject = new BasicInventoryObject(playerView, originalItemStack, slot);
        inventoryObject.setVisible(true);
        inventoryObject.setItemStack(newItemStack);

        inventoryObject.updateInInventory();

        Mockito.verify(playerInventory, Mockito.times(1)).setItemStack(slot, newItemStack);
        Assertions.assertSame(newItemStack, inventoryObject.getItemStack());
    }

    @Test
    public void testInvisibleDoesNotUpdateInInventory() {
        Player player = Mockito.mock(Player.class);
        PlayerInventory playerInventory = Mockito.mock(PlayerInventory.class);
        Mockito.when(player.getInventory()).thenReturn(playerInventory);
        PlayerView playerView = createPlayerView(player);
        ItemStack originalItemStack = ItemStack.of(Material.STICK);
        ItemStack newItemStack = ItemStack.of(Material.DIAMOND);
        int slot = 1;
        InventoryObject inventoryObject = new BasicInventoryObject(playerView, originalItemStack, slot);

        inventoryObject.setVisible(false);
        inventoryObject.setItemStack(newItemStack);

        Assertions.assertThrowsExactly(IllegalStateException.class, inventoryObject::updateInInventory);
    }

    @Test
    public void testEventsCalled() {
        Player player = Mockito.mock(Player.class);
        PlayerInventory playerInventory = Mockito.mock(PlayerInventory.class);
        Mockito.when(player.getInventory()).thenReturn(playerInventory);
        PlayerView playerView = createPlayerView(player);
        ItemStack itemStack = ItemStack.of(Material.STICK);
        int slot = 1;
        InventoryObject inventoryObject = new BasicInventoryObject(playerView, itemStack, slot);
        boolean[] results = new boolean[5];
        inventoryObject.addLeftClickHandler(() -> results[0] = true);
        inventoryObject.addRightClickHandler(() -> results[1] = true);
        inventoryObject.addVisibilityChangedHandler(() -> results[2] = true);
        inventoryObject.addSelectionChangedHandler(() -> results[3] = true);
        inventoryObject.addRemovalHandler(() -> results[4] = true);

        inventoryObject.onLeftClick();
        inventoryObject.onRightClick();
        inventoryObject.setVisible(true);
        inventoryObject.setSelected(true);
        inventoryObject.onRemove();

        for (boolean result : results) {
            Assertions.assertTrue(result);
        }
    }

}
