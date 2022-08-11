package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.InstanceClientBlockHandler;
import com.github.phantazmnetwork.core.gui.BasicSlotDistributor;
import com.github.phantazmnetwork.core.gui.ClickHandler;
import com.github.phantazmnetwork.core.gui.Gui;
import com.github.phantazmnetwork.core.gui.GuiItem;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.phantazmnetwork.core.hologram.ViewableHologram;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

final class ZombiesTest {
    private static boolean holograms = false;

    private ZombiesTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull EventNode<Event> global) {
        global.addListener(PlayerLoginEvent.class, event -> {
            if (holograms) {
                return;
            }

            holograms = true;

            Instance spawnInstance = event.getSpawningInstance();
            if (spawnInstance != null) {
                ClientBlockHandler tracker = new InstanceClientBlockHandler(spawnInstance, global);
                tracker.setClientBlock(Block.BARRIER, 1, 100, 1);

                Hologram hologram = new InstanceHologram(Vec3D.of(1, 101, 1), 0);
                hologram.add(Component.text("This hologram should be...").color(TextColor.color(255, 0, 0)));
                hologram.add(Component.text("...visible to everyone").color(TextColor.color(0, 255, 0)));
                hologram.add(Component.text("And be colored.").color(TextColor.color(0, 0, 255)));
                hologram.setInstance(spawnInstance);

                UUID steankUUID = UUID.fromString("6458e77a-f565-4374-9de7-c2a20be572f3");

                Hologram steankHologram =
                        new ViewableHologram(Vec3D.of(1, 105, 1), 0, player -> player.getUuid().equals(steankUUID));
                steankHologram.add(Component.text("This should only be visible to Steank"));

                Hologram everyoneElseHologram =
                        new ViewableHologram(Vec3D.of(5, 101, 5), 0, player -> !player.getUuid().equals(steankUUID));
                everyoneElseHologram.add(Component.text("This should be visible to everyone EXCEPT Steank"));

                steankHologram.setInstance(spawnInstance);
                everyoneElseHologram.setInstance(spawnInstance);
            }
        });

        global.addListener(EventListener.builder(PlayerChatEvent.class).ignoreCancelled(false).handler(event -> {
            if (event.getMessage().equals("G")) {
                event.getPlayer().openInventory(Gui.builder(InventoryType.CHEST_6_ROW, new BasicSlotDistributor(1))
                        .withItem(GuiItem.builder().withItem(ItemStack.of(Material.SEAGRASS)
                                        .withDisplayName(Component.text("Become beaned").color(TextColor.color(0, 255, 0))))
                                .withClickHandler(((ClickHandler)(owner, player, slot, clickType) -> {
                                    player.sendMessage(Component.text("get beaned you heckin' fool"));
                                    player.closeInventory();
                                }).filter(GuiItem.ClickType.LEFT_CLICK)).build()).build());
            }
        }).build());
    }
}
