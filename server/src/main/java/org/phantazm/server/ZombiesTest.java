package org.phantazm.server;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.gui.*;
import org.phantazm.core.hologram.Hologram;
import org.phantazm.core.hologram.InstanceHologram;
import org.phantazm.core.hologram.ViewableHologram;

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
                Hologram hologram = new InstanceHologram(new Vec(1, 101, 1), 0);
                hologram.add(Component.text("This hologram should be...").color(TextColor.color(255, 0, 0)));
                hologram.add(Component.text("...visible to everyone").color(TextColor.color(0, 255, 0)));
                hologram.add(Component.text("And be colored.").color(TextColor.color(0, 0, 255)));
                hologram.setInstance(spawnInstance);

                UUID steankUUID = UUID.fromString("6458e77a-f565-4374-9de7-c2a20be572f3");

                Hologram steankHologram =
                        new ViewableHologram(new Vec(1, 105, 1), 0, player -> player.getUuid().equals(steankUUID));
                steankHologram.add(Component.text("This should only be visible to Steank"));

                Hologram everyoneElseHologram =
                        new ViewableHologram(new Vec(5, 101, 5), 0, player -> !player.getUuid().equals(steankUUID));
                everyoneElseHologram.add(Component.text("This should be visible to everyone EXCEPT Steank"));

                steankHologram.setInstance(spawnInstance);
                everyoneElseHologram.setInstance(spawnInstance);
            }
        });

        global.addListener(EventListener.builder(PlayerChatEvent.class).ignoreCancelled(false).handler(event -> {
            if (event.getMessage().equals("P")) {
                Component message = Component.text("Play Pigstep", TextColor.color(0, 255, 0));
                Gui gui = Gui.builder(InventoryType.CHEST_6_ROW, new BasicSlotDistributor(1)).setDynamic(true).withItem(
                        GuiItem.builder().withItem(ItemStack.of(Material.MUSIC_DISC_PIGSTEP).withDisplayName(message))
                                .withUpdater(new ItemUpdater() {
                                    private static final Component[] FRAMES =
                                            new Component[] {Component.text("Play Pigstep", TextColor.color(0, 255, 0)),
                                                    Component.text("Play Pigstep.", TextColor.color(0, 0, 255)),
                                                    Component.text("Play Pigstep..", TextColor.color(255, 0, 0)),
                                                    Component.text("Play Pigstep...", TextColor.color(255, 255, 0)),
                                                    Component.text("Play Pigstep....", TextColor.color(255, 0, 255)),
                                                    Component.text("Play Pigstep.....", TextColor.color(0, 255, 255)),
                                                    Component.text("Play Pigstep......",
                                                            TextColor.color(255, 255, 255))};
                                    private long lastUpdate = 0;
                                    private int curFrame = 0;

                                    @Override
                                    public @NotNull ItemStack update(long time, @NotNull ItemStack current) {
                                        ItemStack newStack = current.withDisplayName(FRAMES[curFrame++]);
                                        if (curFrame == FRAMES.length) {
                                            curFrame = 0;
                                        }

                                        return newStack;
                                    }

                                    @Override
                                    public boolean hasUpdate(long time, @NotNull ItemStack current) {
                                        long sinceLastUpdate = time - lastUpdate;
                                        if (sinceLastUpdate > 250) {
                                            lastUpdate = time;
                                            return true;
                                        }

                                        return false;
                                    }
                                }).withClickHandler(((ClickHandler)(owner, player, slot, clickType) -> {
                                    player.sendMessage(Component.text("Playing pigstep."));
                                    pigstepRandomPitch(player);

                                    for (int i = 1; i < 50; i++) {
                                        player.scheduler()
                                                .scheduleTask(() -> pigstepRandomPitch(player), TaskSchedule.millis(100 * i),
                                                        TaskSchedule.stop());
                                    }

                                    player.closeInventory();
                                }).filter(GuiItem.ClickType.LEFT_CLICK)).build()).build();
                event.getPlayer().openInventory(gui);
            }
        }).build());

        global.addListener(PlayerTickEvent.class, event -> {
            Player player = event.getPlayer();
            Inventory inventory = player.getOpenInventory();

            if (inventory instanceof Gui gui) {
                gui.tick(System.currentTimeMillis());
            }
        });
    }

    private static void pigstepRandomPitch(Player player) {
        player.playSound(Sound.sound(Key.key("minecraft:music_disc.pigstep"), Sound.Source.RECORD, 100,
                (float)Math.random() * 2F), Sound.Emitter.self());
    }
}
