package com.github.phantazmnetwork.zombies.game;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.ClientBlockHandler;
import com.github.phantazmnetwork.core.ClientBlockHandlerSource;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.core.game.scene.SceneProviderAbstract;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.instance.InstanceLoader;
import com.github.phantazmnetwork.core.inventory.BasicInventoryProfile;
import com.github.phantazmnetwork.core.inventory.BasicInventoryProfileSwitcher;
import com.github.phantazmnetwork.core.inventory.InventoryProfileSwitcher;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.mob.MobModel;
import com.github.phantazmnetwork.mob.spawner.MobSpawner;
import com.github.phantazmnetwork.zombies.audience.ChatComponentSender;
import com.github.phantazmnetwork.zombies.game.coin.BasicPlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.coin.component.BasicTransactionComponentCreator;
import com.github.phantazmnetwork.zombies.game.kill.BasicPlayerKills;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.player.BasicZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.game.player.state.AlivePlayerState;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import com.github.phantazmnetwork.zombies.game.stage.Stage;
import com.github.phantazmnetwork.zombies.map.MapInfo;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Phaser;
import java.util.function.Function;

public class ZombiesSceneProvider extends SceneProviderAbstract<ZombiesScene, ZombiesJoinRequest> {

    private final MapInfo mapInfo;

    private final InstanceManager instanceManager;

    private final InstanceLoader instanceLoader;

    private final SceneFallback sceneFallback;

    private final MobSpawner mobSpawner;

    private final Map<Key, MobModel> mobModels;

    private final ClientBlockHandlerSource clientBlockHandlerSource;

    public ZombiesSceneProvider(int maximumScenes, @NotNull MapInfo mapInfo, @NotNull InstanceManager instanceManager,
                                @NotNull InstanceLoader instanceLoader, @NotNull SceneFallback sceneFallback,
                                @NotNull MobSpawner mobSpawner, @NotNull Map<Key, MobModel> mobModels,
                                @NotNull ClientBlockHandlerSource clientBlockHandlerSource) {
        super(maximumScenes);
        this.mapInfo = Objects.requireNonNull(mapInfo, "mapInfo");
        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.sceneFallback = Objects.requireNonNull(sceneFallback, "sceneFallback");
        this.mobSpawner = Objects.requireNonNull(mobSpawner, "mobSpawner");
        this.mobModels = Objects.requireNonNull(mobModels, "mobModels");
        this.clientBlockHandlerSource = Objects.requireNonNull(clientBlockHandlerSource, "clientBlockHandlerSource");
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

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected @NotNull ZombiesScene createScene(@NotNull ZombiesJoinRequest request) {
        Instance instance = instanceLoader.loadInstance(instanceManager,
                                                        List.of(mapInfo.settings().id().toString())
        ); // TODO:verify

        Phaser phaser = new Phaser(1);
        Point spawn = VecUtils.toPoint(mapInfo.settings().spawn().add(mapInfo.settings().origin()));
        ChunkUtils.forChunksInRange(spawn, MinecraftServer.getChunkViewDistance(), (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadOptionalChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> phaser.arriveAndDeregister());
        });
        phaser.arriveAndAwaitAdvance();

        Map<UUID, ZombiesPlayer> zombiesPlayers = new HashMap<>(mapInfo.settings().maxPlayers());
        Function<PlayerView, ZombiesPlayer> playerCreator = playerView -> {
            PlayerCoins coins = new BasicPlayerCoins(playerView::getPlayer, new ChatComponentSender(),
                                                     new BasicTransactionComponentCreator(), 0
            );
            PlayerKills kills = new BasicPlayerKills();
            InventoryProfileSwitcher profileSwitcher = new BasicInventoryProfileSwitcher();
            Key profileKey = Key.key(Namespaces.PHANTAZM, "inventory.profile.default");
            profileSwitcher.registerProfile(profileKey, new BasicInventoryProfile(9));
            profileSwitcher.switchProfile(profileKey);
            ZombiesPlayerState defaultState = new AlivePlayerState(playerView, player -> {
                player.setFlying(false);
                player.setAllowFlying(false);
                player.setGameMode(GameMode.ADVENTURE);
                player.setInvisible(false);
            });
            PlayerStateSwitcher stateSwitcher = new PlayerStateSwitcher(defaultState);

            return new BasicZombiesPlayer(playerView, coins, kills, profileSwitcher, stateSwitcher);
        };
        Random random = new Random();
        ClientBlockHandler blockHandler = clientBlockHandlerSource.forInstance(instance);
        /*ComponentBuilder componentBuilder = new BasicComponentBuilder();
        SpawnDistributor spawnDistributor = new BasicSpawnDistributor(mobModels::get, , random);
        ZombiesMap map = new ZombiesMap(mapInfo, componentBuilder, instance, mobSpawner, blockHandler, );
        StageTransition stageTransition = new StageTransition(List.of(
                new IdleStage(zombiesPlayers),
                new CountdownStage(zombiesPlayers, 200L),
                new InGameStage(zombiesPlayers, map),
                new EndGameStage(zombiesPlayers, 100L)
        ));

        return new ZombiesScene(zombiesPlayers, instance, sceneFallback, mapInfo.settings(), stageTransition,
                                playerCreator, random);*/
        return null; // TODO fix commented code
    }
}
