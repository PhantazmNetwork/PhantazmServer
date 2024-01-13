package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.FileUtils;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.BasicClientBlockHandlerSource;
import org.phantazm.core.ClientBlockHandlerSource;
import org.phantazm.core.VecUtils;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.instance.AnvilFileSystemInstanceLoader;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.loader.DataSource;
import org.phantazm.loader.ObjectExtractor;
import org.phantazm.mob2.MobCreator;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.server.context.*;
import org.phantazm.stats.zombies.JDBCZombiesStatsDatabase;
import org.phantazm.stats.zombies.ZombiesStatsDatabase;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.command.ZombiesCommand;
import org.phantazm.zombies.corpse.CorpseCreator;
import org.phantazm.zombies.endless.Endless;
import org.phantazm.zombies.map.FileSystemMapLoader;
import org.phantazm.zombies.map.Loader;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.mob2.BasicMobSpawnerSource;
import org.phantazm.zombies.mob2.MobSpawnerSource;
import org.phantazm.zombies.modifier.*;
import org.phantazm.zombies.player.BasicZombiesPlayerSource;
import org.phantazm.zombies.powerup.BasicPowerupHandlerSource;
import org.phantazm.zombies.powerup.FileSystemPowerupDataLoader;
import org.phantazm.zombies.powerup.PowerupData;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.scene2.ZombiesJoiner;
import org.phantazm.zombies.scene2.ZombiesLeaderboardContext;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.scene2.ZombiesSceneCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ZombiesFeature {
    public static final Path MAPS_FOLDER = Path.of("./zombies/maps");
    public static final Path POWERUPS_FOLDER = Path.of("./zombies/powerups");
    public static final Path INSTANCES_FOLDER = Path.of("./zombies/instances");
    public static final Path MODIFIERS_FOLDER = Path.of("./zombies/modifiers");

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);

    private static Map<Key, MapInfo> maps;

    private static PowerupHandler.Source powerupHandlerSource;

    private static MobSpawnerSource mobSpawnerSource;
    private static ZombiesStatsDatabase database;

    static void initialize(@NotNull ConfigContext configContext, @NotNull EthyleneContext ethyleneContext,
        @NotNull DatabaseAccessContext databaseAccessContext,
        @NotNull DataLoadingContext dataLoadingContext, @NotNull PlayerContext playerContext,
        @NotNull GameContext gameContext) {

        MappingProcessorSource mappingProcessorSource = ethyleneContext.mappingProcessorSource();
        ContextManager contextManager = dataLoadingContext.contextManager();
        Supplier<? extends Map<Key, MobCreator>> mobCreatorMap = gameContext.mobCreatorSupplier();
        ZombiesConfig zombiesConfig = configContext.zombiesConfig();
        SongLoader songLoader = gameContext.songLoader();
        KeyParser keyParser = ethyleneContext.keyParser();
        Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSettingsFunction =
            gameContext.instanceSettingsFunction();
        Map<? super UUID, ? extends Party> parties = playerContext.parties();
        ModifierCommandConfig commandConfig = configContext.modifierCommandConfig();


        try {
            FileUtils.createDirectories(MAPS_FOLDER);
            FileUtils.createDirectories(POWERUPS_FOLDER);
            FileUtils.createDirectories(INSTANCES_FOLDER);
            FileUtils.createDirectories(MODIFIERS_FOLDER);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Attributes.registerAll();

        ConfigCodec codec = new YamlCodec();
        ZombiesFeature.maps = loadFeature("map", new FileSystemMapLoader(MAPS_FOLDER, codec, mappingProcessorSource));

        ZombiesFeature.powerupHandlerSource = new BasicPowerupHandlerSource(loadFeature("powerup",
            new FileSystemPowerupDataLoader(POWERUPS_FOLDER, codec,
                mappingProcessorSource.processorFor(Token.ofClass(PowerupData.class)))), contextManager);

        ZombiesFeature.mobSpawnerSource = new BasicMobSpawnerSource(mobCreatorMap);

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

        Map<Key, SceneCreator<ZombiesScene>> providers = new HashMap<>(maps.size());

        database = new JDBCZombiesStatsDatabase(ExecutorFeature.getExecutor(), HikariFeature.getDataSource());
        database.initTables();

        EventNode<Event> globalEventNode = MinecraftServer.getGlobalEventHandler();
        ClientBlockHandlerSource clientBlockHandlerSource = new BasicClientBlockHandlerSource();

        for (Map.Entry<Key, MapInfo> entry : maps.entrySet()) {
            CorpseCreator.Source corpseCreatorSource = mapDependencyProvider -> contextManager
                .makeContext(entry.getValue().corpse()).provide(mapDependencyProvider);

            Endless.Source endlessSource = dependencyProvider -> contextManager
                .makeContext(entry.getValue().endless()).provide(dependencyProvider);

            ZombiesLeaderboardContext leaderboardContext =
                new ZombiesLeaderboardContext(ExecutorFeature.getExecutor(),
                    databaseAccessContext.zombiesLeaderboardDatabase(),
                    contextManager.makeContext(entry.getValue().leaderboard()).provide());

            ZombiesSceneCreator provider = new ZombiesSceneCreator(zombiesConfig.maximumScenes(), entry.getValue(),
                instanceLoader, keyParser, contextManager, songLoader, database,
                instanceSettingsFunction, globalEventNode, ZombiesFeature.mobSpawnerSource(), clientBlockHandlerSource,
                ZombiesFeature.powerupHandlerSource(), new BasicZombiesPlayerSource(EquipmentFeature::createEquipmentCreator),
                corpseCreatorSource, endlessSource, leaderboardContext, PermissionFeature.roleStore(), IdentitySource.MOJANG);
            providers.put(entry.getKey(), provider);
        }

        ConfigProcessor<ModifierData> dataConfigProcessor = mappingProcessorSource
            .processorFor(Token.ofClass(ModifierData.class));

        org.phantazm.loader.Loader<ModifierComponent> componentLoader = org.phantazm.loader.Loader.loader(() ->
                DataSource.directory(MODIFIERS_FOLDER, codec, "glob:**.{yml,yaml}"),
            ObjectExtractor.extractor(ConfigNode.class, (location, node) -> {
                ModifierData modifierData = dataConfigProcessor.dataFromElement(node);

                ElementContext context = contextManager.makeContext(modifierData.modifier());
                DualComponent<ZombiesScene, Modifier> modifierComponent = context.provide(DependencyProvider.EMPTY,
                    ElementContext.DEFAULT_EXCEPTION_HANDLER, () -> null);

                return List.of(ObjectExtractor.entry(modifierData.key(), new ModifierSource(modifierData, modifierComponent)));
            }));

        componentLoader.loadUnchecked();

        LOGGER.info("Loaded {} modifiers", componentLoader.data().size());
        ModifierHandler.Global.init(componentLoader.data(), InjectionStore.of());

        ZombiesJoiner joiner = new ZombiesJoiner(providers);

        MinecraftServer.getCommandManager().register(new ZombiesCommand(joiner, parties, keyParser, maps,
            zombiesConfig.joinRatelimit(), database, commandConfig));
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

    public static @NotNull ZombiesStatsDatabase getDatabase() {
        return FeatureUtils.check(database);
    }

    private record PreloadedMap(@NotNull List<String> instancePath,
        @NotNull Point spawn,
        int chunkLoadRange) {

    }

}
