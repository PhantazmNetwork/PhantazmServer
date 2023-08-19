package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import it.unimi.dsi.fastutil.booleans.BooleanObjectPair;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.BasicClientBlockHandlerSource;
import org.phantazm.core.ClientBlockHandlerSource;
import org.phantazm.core.VecUtils;
import org.phantazm.core.game.scene.SceneTransferHelper;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.instance.AnvilFileSystemInstanceLoader;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.proxima.bindings.minestom.Spawner;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.stats.zombies.SQLZombiesDatabase;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.command.ZombiesCommand;
import org.phantazm.zombies.map.FileSystemMapLoader;
import org.phantazm.zombies.map.Loader;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.mob.BasicMobSpawnerSource;
import org.phantazm.zombies.mob.MobSpawnerSource;
import org.phantazm.zombies.player.BasicZombiesPlayerSource;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.BasicPowerupHandlerSource;
import org.phantazm.zombies.powerup.FileSystemPowerupDataLoader;
import org.phantazm.zombies.powerup.PowerupData;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.scene.ZombiesSceneProvider;
import org.phantazm.zombies.scene.ZombiesSceneRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class ZombiesFeature {
    public static final Path MAPS_FOLDER = Path.of("./zombies/maps");
    public static final Path POWERUPS_FOLDER = Path.of("./zombies/powerups");
    public static final Path INSTANCES_FOLDER = Path.of("./zombies/instances");

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);

    private static Map<Key, MapInfo> maps;

    private static PowerupHandler.Source powerupHandlerSource;

    private static MobSpawnerSource mobSpawnerSource;
    private static ZombiesSceneRouter sceneRouter;
    private static ZombiesDatabase database;

    static void initialize(@NotNull ContextManager contextManager,
        @NotNull Map<BooleanObjectPair<String>, ConfigProcessor<?>> processorMap, @NotNull Spawner spawner,
        @NotNull KeyParser keyParser,
        @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction,
        @NotNull PlayerViewProvider viewProvider, @NotNull SceneFallback sceneFallback,
        @NotNull Map<? super UUID, ? extends Party> parties, @NotNull SceneTransferHelper sceneTransferHelper,
        @NotNull SongLoader songLoader, @NotNull ZombiesConfig zombiesConfig,
        @NotNull MappingProcessorSource mappingProcessorSource) {
        Attributes.registerAll();

        ConfigCodec codec = new YamlCodec();
        ZombiesFeature.maps = loadFeature("map", new FileSystemMapLoader(MAPS_FOLDER, codec, mappingProcessorSource));

        ZombiesFeature.powerupHandlerSource = new BasicPowerupHandlerSource(loadFeature("powerup",
            new FileSystemPowerupDataLoader(POWERUPS_FOLDER, codec,
                mappingProcessorSource.processorFor(Token.ofClass(PowerupData.class)))), contextManager);

        ZombiesFeature.mobSpawnerSource = new BasicMobSpawnerSource(processorMap, spawner, keyParser);

        InstanceLoader instanceLoader =
            new AnvilFileSystemInstanceLoader(MinecraftServer.getInstanceManager(), INSTANCES_FOLDER,
                DynamicChunk::new, ExecutorFeature.getExecutor());

        Set<PreloadedMap> instancePaths = new HashSet<>(maps.size());
        for (MapInfo mapInfo : maps.values()) {
            instancePaths.add(new PreloadedMap(mapInfo.settings().instancePath(),
                VecUtils.toPoint(mapInfo.settings().origin().add(mapInfo.settings().spawn())),
                mapInfo.settings().chunkLoadRange()));
        }

        LOGGER.info("Preloading {} map instances", instancePaths.size());
        List<CompletableFuture<Void>> loadFutures = new ArrayList<>(instancePaths.size());
        for (PreloadedMap map : instancePaths) {
            loadFutures.add(CompletableFuture.runAsync(() -> {
                instanceLoader.preload(map.instancePath, map.spawn, map.chunkLoadRange);
            }));
        }

        CompletableFuture.allOf(loadFutures.toArray(CompletableFuture[]::new)).join();

        Map<Key, ZombiesSceneProvider> providers = new HashMap<>(maps.size());

        database = new SQLZombiesDatabase(ExecutorFeature.getExecutor(), HikariFeature.getDataSource());

        EventNode<Event> globalEventNode = MinecraftServer.getGlobalEventHandler();
        ClientBlockHandlerSource clientBlockHandlerSource = new BasicClientBlockHandlerSource(globalEventNode);
        for (Map.Entry<Key, MapInfo> entry : maps.entrySet()) {
            ZombiesSceneProvider provider =
                new ZombiesSceneProvider(ExecutorFeature.getExecutor(), zombiesConfig.maximumScenes(),
                    instanceSpaceFunction, entry.getValue(), instanceLoader, sceneFallback, globalEventNode,
                    ZombiesFeature.mobSpawnerSource(), MobFeature.getModels(), clientBlockHandlerSource,
                    contextManager, keyParser, database, viewProvider, ZombiesFeature.powerupHandlerSource(),
                    new BasicZombiesPlayerSource(database, ExecutorFeature.getExecutor(), viewProvider,
                        EquipmentFeature::createEquipmentCreator, MobFeature.getModels(), contextManager,
                        keyParser),
                    mapDependencyProvider -> contextManager.makeContext(entry.getValue().corpse())
                        .provide(mapDependencyProvider), songLoader);
            providers.put(entry.getKey(), provider);
        }

        ZombiesFeature.sceneRouter = new ZombiesSceneRouter(providers);

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            sceneRouter.tick(System.currentTimeMillis());
        }, TaskSchedule.immediate(), TaskSchedule.nextTick());

        MinecraftServer.getCommandManager().register(
            new ZombiesCommand(parties, sceneRouter, keyParser, maps, viewProvider,
                MinecraftServer.getSchedulerManager(), sceneTransferHelper, sceneFallback,
                zombiesConfig.joinRatelimit()));
    }

    private static <T extends Keyed> Map<Key, T> loadFeature(String featureName, Loader<T> loader) {
        List<String> dataNames;
        try {
            dataNames = loader.loadableData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<Key, T> data = new HashMap<>(dataNames.size());
        for (String dataName : dataNames) {
            try {
                T feature = loader.load(dataName);
                Key id = feature.key();

                if (data.containsKey(id)) {
                    LOGGER.warn("Found duplicate " + featureName + " with id " + id + "; the previously loaded " +
                        "version will be overwritten");
                }

                data.put(id, feature);
            } catch (IOException e) {
                LOGGER.warn("Exception when loading " + featureName, e);
            }
        }

        LOGGER.info("Loaded " + data.size() + " " + featureName + "s");
        return Map.copyOf(data);
    }

    public static @NotNull @Unmodifiable Map<Key, MapInfo> maps() {
        return FeatureUtils.check(maps);
    }

    public static @NotNull PowerupHandler.Source powerupHandlerSource() {
        return FeatureUtils.check(powerupHandlerSource);
    }

    public static @NotNull MobSpawnerSource mobSpawnerSource() {
        return FeatureUtils.check(mobSpawnerSource);
    }

    public static @NotNull Optional<ZombiesScene> getPlayerScene(@NotNull UUID playerUUID) {
        return FeatureUtils.check(sceneRouter).getCurrentScene(playerUUID);
    }

    public static @NotNull Optional<ZombiesPlayer> getZombiesPlayer(@NotNull UUID playerUUID) {
        return FeatureUtils.check(sceneRouter).getCurrentScene(playerUUID)
            .map(scene -> scene.getZombiesPlayers().get(playerUUID));
    }

    public static @NotNull ZombiesSceneRouter zombiesSceneRouter() {
        return FeatureUtils.check(sceneRouter);
    }

    public static @NotNull ZombiesDatabase getDatabase() {
        return FeatureUtils.check(database);
    }

    private record PreloadedMap(@NotNull List<String> instancePath,
        @NotNull Point spawn,
        int chunkLoadRange) {

    }

}
