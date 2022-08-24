package com.github.phantazmnetwork.server;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.vector.Vec3D;
import com.github.phantazmnetwork.core.BasicClientBlockHandlerSource;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.InstanceClientBlockHandler;
import com.github.phantazmnetwork.core.game.scene.fallback.KickFallback;
import com.github.phantazmnetwork.core.gui.*;
import com.github.phantazmnetwork.core.hologram.Hologram;
import com.github.phantazmnetwork.core.hologram.InstanceHologram;
import com.github.phantazmnetwork.core.hologram.ViewableHologram;
import com.github.phantazmnetwork.core.instance.AnvilFileSystemInstanceLoader;
import com.github.phantazmnetwork.core.instance.InstanceLoader;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.core.player.PlayerViewProvider;
import com.github.phantazmnetwork.neuron.bindings.minestom.chunk.NeuralChunk;
import com.github.phantazmnetwork.zombies.game.ZombiesJoinRequest;
import com.github.phantazmnetwork.zombies.game.ZombiesRouteRequest;
import com.github.phantazmnetwork.zombies.game.ZombiesSceneProvider;
import com.github.phantazmnetwork.zombies.game.ZombiesSceneRouter;
import com.github.phantazmnetwork.zombies.map.FileSystemMapLoader;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.phantazmnetwork.zombies.map.MapLoader;
import com.github.steanky.element.core.*;
import com.github.steanky.element.core.context.BasicContextManager;
import com.github.steanky.element.core.context.BasicElementContext;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.data.BasicDataInspector;
import com.github.steanky.element.core.data.BasicDataLocator;
import com.github.steanky.element.core.data.DataInspector;
import com.github.steanky.element.core.data.DataLocator;
import com.github.steanky.element.core.factory.BasicFactoryResolver;
import com.github.steanky.element.core.factory.FactoryResolver;
import com.github.steanky.element.core.key.*;
import com.github.steanky.element.core.processor.BasicProcessorResolver;
import com.github.steanky.element.core.processor.ProcessorResolver;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerChatEvent;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerTickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

final class ZombiesTest {
    private static boolean holograms = false;

    private ZombiesTest() {
        throw new UnsupportedOperationException();
    }

    static void initialize(@NotNull PlayerViewProvider viewProvider, @NotNull EventNode<Event> global) {
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

        MapLoader mapLoader = new FileSystemMapLoader(Path.of("./zombies/maps"), new YamlCodec());
        MapInfo mapInfo;
        try {
            mapInfo = mapLoader.load("map_de");
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        InstanceLoader instanceLoader =
                new AnvilFileSystemInstanceLoader(Path.of("./zombies/instances"), NeuralChunk::new);
        KeyParser keyParser = new BasicKeyParser(Namespaces.PHANTAZM);
        ElementTypeIdentifier typeIdentifier = new BasicElementTypeIdentifier(keyParser);
        DataInspector dataInspector = new BasicDataInspector(keyParser);
        FactoryResolver factoryResolver = new BasicFactoryResolver(keyParser, typeIdentifier, dataInspector);
        ProcessorResolver processorResolver = new BasicProcessorResolver();
        ElementInspector elementInspector = new BasicElementInspector(factoryResolver, processorResolver);
        KeyExtractor keyExtractor = new BasicKeyExtractor("serialKey", keyParser);
        PathSplitter pathSplitter = new BasicPathSplitter();
        DataLocator dataLocator = new BasicDataLocator(pathSplitter);
        Registry<ConfigProcessor<?>> processorRegistry = new HashRegistry<>();
        Registry<ElementFactory<?, ?>> factoryRegistry = new HashRegistry<>();
        ElementContext.Source source =
                new BasicElementContext.Source(processorRegistry, factoryRegistry, pathSplitter, dataLocator,
                        keyExtractor);
        ContextManager contextManager = new BasicContextManager(elementInspector, typeIdentifier, source);
        ZombiesSceneProvider sceneProvider =
                new ZombiesSceneProvider(1, mapInfo, MinecraftServer.getInstanceManager(), instanceLoader,
                        new KickFallback(Component.text("idk how")), global, Mob.getMobStore(), Mob.getMobSpawner(),
                        Mob.getModels(),
                        new BasicClientBlockHandlerSource(instance -> new InstanceClientBlockHandler(instance, global)),
                        contextManager, keyParser);
        ZombiesSceneRouter sceneRouter =
                new ZombiesSceneRouter(Map.of(Key.key(Namespaces.PHANTAZM, "map_de"), sceneProvider));
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
            else if (event.getMessage().equals("go map_de")) {
                sceneRouter.join(
                        new ZombiesRouteRequest(Key.key(Namespaces.PHANTAZM, "map_de"), new ZombiesJoinRequest() {
                            @Override
                            public @NotNull Collection<PlayerView> getPlayers() {
                                return Collections.singleton(viewProvider.fromPlayer(event.getPlayer()));
                            }

                            @Override
                            public int getRequestWeight() {
                                return 1;
                            }
                        }));
            }
        }).build());

        global.addListener(PlayerTickEvent.class, event -> {
            Player player = event.getPlayer();
            Inventory inventory = player.getOpenInventory();

            if (inventory instanceof Gui gui) {
                gui.tick(System.currentTimeMillis());
            }
        });
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            sceneRouter.tick(System.currentTimeMillis());
        }, TaskSchedule.immediate(), TaskSchedule.tick(1));
    }

    private static void pigstepRandomPitch(Player player) {
        player.playSound(Sound.sound(Key.key("minecraft:music_disc.pigstep"), Sound.Source.RECORD, 100,
                (float)Math.random() * 2F), Sound.Emitter.self());
    }
}
