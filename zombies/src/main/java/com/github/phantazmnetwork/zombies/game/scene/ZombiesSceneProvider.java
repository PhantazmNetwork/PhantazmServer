package com.github.phantazmnetwork.zombies.game.scene;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.commons.Wrapper;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.ClientBlockHandlerSource;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.core.game.scene.SceneProviderAbstract;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.gui.BasicSlotDistributor;
import com.github.phantazmnetwork.core.gui.SlotDistributor;
import com.github.phantazmnetwork.core.instance.InstanceLoader;
import com.github.phantazmnetwork.core.inventory.*;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.audience.ChatComponentSender;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.equipment.gun.ZombiesGunModule;
import com.github.phantazmnetwork.zombies.equipment.gun.audience.PlayerAudienceProvider;
import com.github.phantazmnetwork.zombies.game.coin.BasicModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.BasicPlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.component.BasicTransactionComponentCreator;
import com.github.phantazmnetwork.zombies.game.kill.BasicPlayerKills;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.map.BasicFlaggable;
import com.github.phantazmnetwork.zombies.game.map.BasicRoundHandler;
import com.github.phantazmnetwork.zombies.game.map.Flaggable;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.game.map.objects.BasicMapObjectBuilder;
import com.github.phantazmnetwork.zombies.game.map.objects.MapObjects;
import com.github.phantazmnetwork.zombies.game.player.BasicZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayerMeta;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayerModule;
import com.github.phantazmnetwork.zombies.game.player.state.BasicZombiesPlayerState;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateKey;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import com.github.phantazmnetwork.zombies.game.player.state.context.NoContext;
import com.github.phantazmnetwork.zombies.game.spawn.BasicSpawnDistributor;
import com.github.phantazmnetwork.zombies.game.spawn.SpawnDistributor;
import com.github.phantazmnetwork.zombies.game.stage.*;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.scoreboard.Sidebar;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Phaser;
import java.util.function.Function;

public class ZombiesSceneProvider extends SceneProviderAbstract<ZombiesScene, ZombiesJoinRequest> {

    private record SceneContext(@NotNull EventNode<?> node) {

        public SceneContext {
            Objects.requireNonNull(node, "node");
        }

    }

    private final IdentityHashMap<ZombiesScene, SceneContext> contexts;

    private final MapInfo mapInfo;

    private final InstanceManager instanceManager;

    private final InstanceLoader instanceLoader;

    private final SceneFallback sceneFallback;

    private final EventNode<Event> eventNode;

    private final MobSpawner mobSpawner;

    private final MobStore mobStore;

    private final Map<Key, MobModel> mobModels;

    private final ClientBlockHandlerSource clientBlockHandlerSource;
    private final ContextManager contextManager;
    private final KeyParser keyParser;

    private final Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction;

    public ZombiesSceneProvider(int maximumScenes, @NotNull MapInfo mapInfo, @NotNull InstanceManager instanceManager,
            @NotNull InstanceLoader instanceLoader, @NotNull SceneFallback sceneFallback,
            @NotNull EventNode<Event> eventNode, @NotNull MobSpawner mobSpawner, @NotNull MobStore mobStore,
            @NotNull Map<Key, MobModel> mobModels, @NotNull ClientBlockHandlerSource clientBlockHandlerSource,
            @NotNull ContextManager contextManager, @NotNull KeyParser keyParser,
            @NotNull Function<ZombiesGunModule, EquipmentCreator> equipmentCreatorFunction) {
        super(maximumScenes);
        this.contexts = new IdentityHashMap<>(maximumScenes);
        this.mapInfo = Objects.requireNonNull(mapInfo, "mapInfo");
        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.sceneFallback = Objects.requireNonNull(sceneFallback, "sceneFallback");
        this.eventNode = Objects.requireNonNull(eventNode, "eventNode");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
        this.mobModels = Objects.requireNonNull(mobModels, "mobModels");
        this.clientBlockHandlerSource = Objects.requireNonNull(clientBlockHandlerSource, "clientBlockHandlerSource");
        this.contextManager = Objects.requireNonNull(contextManager, "contextManager");
        this.keyParser = Objects.requireNonNull(keyParser, "keyParser");
        this.equipmentCreatorFunction = Objects.requireNonNull(equipmentCreatorFunction, "equipmentCreatorFunction");
    }

    @Override
    protected @NotNull Optional<ZombiesScene> chooseScene(@NotNull ZombiesJoinRequest request) {
        for (ZombiesScene scene : getScenes()) {
            if (scene.isComplete() || !scene.isJoinable() || scene.isShutdown()) {
                continue;
            }

            Stage stage = scene.getCurrentStage();
            if (stage == null || stage.hasPermanentPlayers()) {
                continue;
            }

            int maxPlayers = scene.getMapSettingsInfo().maxPlayers();
            int currentPlayerCount = scene.getZombiesPlayers().size();

            if (currentPlayerCount + request.getPlayers().size() <= maxPlayers) {
                return Optional.of(scene);
            }
        }

        return Optional.empty();
    }

    @Override
    protected @NotNull ZombiesScene createScene(@NotNull ZombiesJoinRequest request) {
        Instance instance = instanceLoader.loadInstance(instanceManager, mapInfo.settings().instancePath());

        Phaser phaser = new Phaser(1);
        Point spawn = VecUtils.toPoint(mapInfo.settings().spawn().add(mapInfo.settings().origin()));
        ChunkUtils.forChunksInRange(spawn, MinecraftServer.getChunkViewDistance(), (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadOptionalChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> phaser.arriveAndDeregister());
        });
        phaser.arriveAndAwaitAdvance();

        Map<UUID, ZombiesPlayer> zombiesPlayers = new HashMap<>(mapInfo.settings().maxPlayers());

        ModifierSource modifierSource = new BasicModifierSource();
        Flaggable flaggable = new BasicFlaggable();
        Random random = new Random();
        MapObjects mapObjects = createMapObjects(instance, random, zombiesPlayers, flaggable, modifierSource, mapInfo);
        ZombiesMap map = new ZombiesMap(mapInfo, instance, modifierSource, flaggable, mapObjects,
                new BasicRoundHandler(mapObjects.rounds()));

        Stage idle = new IdleStage(Collections.emptyList(), zombiesPlayers.values());
        Stage countdown = new CountdownStage(Collections.emptyList(), zombiesPlayers.values(), Wrapper.of(400L), 400L);
        Stage inGame = new InGameStage(Collections.emptyList(), map, Wrapper.of(0L));
        Stage end = new EndGameStage(Collections.emptyList(), zombiesPlayers.values(), 30L);
        StageTransition stageTransition = new StageTransition(List.of(idle, countdown, inGame, end));
        Function<? super PlayerView, ? extends ZombiesPlayer> playerCreator = playerView -> {
            ZombiesPlayerModule module = createPlayerModule(playerView, modifierSource, random, mapObjects);
            return new BasicZombiesPlayer(module);
        };
        return new ZombiesScene(zombiesPlayers, instance, sceneFallback, mapInfo.settings(), stageTransition,
                playerCreator, random);
    }

    private @NotNull MapObjects createMapObjects(@NotNull Instance instance, @NotNull Random random,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers, @NotNull Flaggable flaggable,
            @NotNull ModifierSource modifierSource, @NotNull MapInfo mapInfo) {
        ClientBlockHandler blockHandler = clientBlockHandlerSource.forInstance(instance);
        SpawnDistributor spawnDistributor = new BasicSpawnDistributor(mobModels::get, random, zombiesPlayers.values());
        SlotDistributor slotDistributor = new BasicSlotDistributor(1);
        Pos spawnPos = VecUtils.toPos(mapInfo.settings().origin().add(mapInfo.settings().spawn()));

        return new BasicMapObjectBuilder(contextManager, instance, mobSpawner, blockHandler, spawnDistributor, null,
                flaggable, modifierSource, slotDistributor, zombiesPlayers, spawnPos, keyParser).build(mapInfo);
    }

    private @NotNull ZombiesPlayerModule createPlayerModule(@NotNull PlayerView playerView,
            @NotNull ModifierSource modifierSource, @NotNull Random random, @NotNull MapObjects mapObjects) {
        ZombiesPlayerMeta meta = new ZombiesPlayerMeta();
        PlayerCoins coins = new BasicPlayerCoins(new PlayerAudienceProvider(playerView), new ChatComponentSender(),
                new BasicTransactionComponentCreator(), 0);
        PlayerKills kills = new BasicPlayerKills();
        InventoryProfile profile = new BasicInventoryProfile(9);
        InventoryAccess inventoryAccess = new InventoryAccess(profile,
                Map.of(Key.key(Namespaces.PHANTAZM, "inventory.access.gun"),
                        new InventoryObjectGroupAbstract(profile, IntSet.of(1, 2)) {
                            @Override
                            public void pushInventoryObject(@NotNull InventoryObject toPush) {
                                for (int slot : getSlots()) {
                                    if (!profile.hasInventoryObject(slot)) {
                                        profile.setInventoryObject(slot, toPush);
                                        return;
                                    }
                                }

                                throw new IllegalStateException("All slots are full");
                            }

                            @Override
                            public @NotNull InventoryObject popInventoryObject() {
                                for (int i = getSlots().size(); i >= 0; i--) {
                                    if (profile.hasInventoryObject(i)) {
                                        InventoryObject object = profile.getInventoryObject(i);
                                        profile.removeInventoryObject(i);
                                        return object;
                                    }
                                }

                                throw new IllegalStateException("All slots are empty");
                            }
                        }));
        InventoryAccessRegistry accessRegistry = new BasicInventoryAccessRegistry();
        Key accessKey = Key.key(Namespaces.PHANTAZM, "inventory.access.default");
        accessRegistry.registerAccess(accessKey, inventoryAccess);
        accessRegistry.switchAccess(accessKey);
        EquipmentHandler equipmentHandler = new EquipmentHandler(accessRegistry);

        ZombiesGunModule gunModule = new ZombiesGunModule(playerView, mobSpawner, mobStore, random, mapObjects);
        EquipmentCreator equipmentCreator = equipmentCreatorFunction.apply(gunModule);

        Key aliveStateKey = Key.key(Namespaces.PHANTAZM, "zombies.player.state.alive");
        Function<NoContext, ZombiesPlayerState> aliveStateCreator =
                unused -> new BasicZombiesPlayerState(Component.text("ALIVE", NamedTextColor.GREEN), aliveStateKey,
                        Collections.emptyList());
        PlayerStateSwitcher stateSwitcher = new PlayerStateSwitcher(aliveStateCreator.apply(NoContext.INSTANCE));
        Map<PlayerStateKey<?>, Function<?, ZombiesPlayerState>> stateFunctions =
                Map.of(new PlayerStateKey<>(aliveStateKey), aliveStateCreator);

        Sidebar sidebar = new Sidebar(Component.text("ZOMBIES", NamedTextColor.RED));

        return new ZombiesPlayerModule(playerView, meta, coins, kills, equipmentHandler, equipmentCreator,
                accessRegistry, stateSwitcher, stateFunctions, sidebar, modifierSource);
    }

    @Override
    protected void cleanupScene(@NotNull ZombiesScene scene) {
        SceneContext context = contexts.remove(scene);
        if (context == null) {
            return;
        }

        eventNode.removeChild(context.node());
    }
}
