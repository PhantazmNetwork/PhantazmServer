package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.codec.yaml.YamlCodec;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
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
import org.phantazm.core.player.PlayerViewProvider;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.mob2.MobCreator;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.stats.zombies.SQLZombiesDatabase;
import org.phantazm.stats.zombies.ZombiesDatabase;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.command.ZombiesCommand;
import org.phantazm.zombies.corpse.CorpseCreator;
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
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.scene2.ZombiesSceneCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ZombiesFeature {
    public static final Path MAPS_FOLDER = Path.of("./zombies/maps");
    public static final Path POWERUPS_FOLDER = Path.of("./zombies/powerups");
    public static final Path INSTANCES_FOLDER = Path.of("./zombies/instances");
    public static final Path MODIFIERS_FOLDER = Path.of("./zombies/modifiers");

    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);

    private static Map<Key, MapInfo> maps;

    private static PowerupHandler.Source powerupHandlerSource;

    private static MobSpawnerSource mobSpawnerSource;
    private static ZombiesDatabase database;

    static void initialize(@NotNull ContextManager contextManager,
        @NotNull KeyParser keyParser,
        @NotNull Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSpaceFunction,
        @NotNull PlayerViewProvider viewProvider,
        @NotNull Map<? super UUID, ? extends Party> parties,
        @NotNull SongLoader songLoader, @NotNull ZombiesConfig zombiesConfig,
        @NotNull MappingProcessorSource mappingProcessorSource, @NotNull Map<Key, MobCreator> mobCreatorMap,
        @NotNull ModifierCommandConfig commandConfig) {
        try {
            FileUtils.createDirectories(MAPS_FOLDER);
            FileUtils.createDirectories(POWERUPS_FOLDER);
            FileUtils.createDirectories(INSTANCES_FOLDER);
            FileUtils.createDirectories(MODIFIERS_FOLDER);
        } catch (IOException e) {
            LOGGER.error("Error creating some directories", e);
            throw new RuntimeException();
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

        database = new SQLZombiesDatabase(ExecutorFeature.getExecutor(), HikariFeature.getDataSource());

        EventNode<Event> globalEventNode = MinecraftServer.getGlobalEventHandler();
        ClientBlockHandlerSource clientBlockHandlerSource = new BasicClientBlockHandlerSource();
        for (Map.Entry<Key, MapInfo> entry : maps.entrySet()) {
            CorpseCreator.Source corpseCreatorSource = mapDependencyProvider -> contextManager
                .makeContext(entry.getValue().corpse()).provide(mapDependencyProvider);

            ZombiesSceneCreator provider =
                new ZombiesSceneCreator(zombiesConfig.maximumScenes(),
                    entry.getValue(), instanceLoader, keyParser, contextManager, songLoader, database, instanceSpaceFunction, globalEventNode,
                    ZombiesFeature.mobSpawnerSource(), clientBlockHandlerSource,
                    ZombiesFeature.powerupHandlerSource(),
                    new BasicZombiesPlayerSource(database, ExecutorFeature.getExecutor(), viewProvider,
                        EquipmentFeature::createEquipmentCreator, contextManager,
                        keyParser),
                    corpseCreatorSource);
            providers.put(entry.getKey(), provider);
        }

        ConfigProcessor<ModifierData> dataConfigProcessor = mappingProcessorSource
            .processorFor(Token.ofClass(ModifierData.class));

        Map<Key, ModifierComponent> modifierComponents = new HashMap<>();
        try (Stream<Path> files = Files.list(MODIFIERS_FOLDER)) {
            for (Path path : (Iterable<Path>) files::iterator) {
                ConfigElement config = Configuration.read(path, codec);
                if (!config.isNode()) {
                    LOGGER.error("Expected top-level node in {}", path);
                    continue;
                }

                ConfigNode node = config.asNode();
                ModifierData modifierData = dataConfigProcessor.dataFromElement(node);

                ElementContext context = contextManager.makeContext(modifierData.modifier());
                DualComponent<ZombiesScene, Modifier> modifierComponent = context.provide(DependencyProvider.EMPTY, exception -> {
                    LOGGER.warn("Error loading modifier in {}: {}", path, exception);
                }, () -> null);
                if (modifierComponent == null) {
                    continue;
                }

                modifierComponents.put(modifierData.key(), new ModifierSource(modifierData, modifierComponent));
            }
        } catch (IOException e) {
            LOGGER.error("Error enumerating modifiers folder", e);
            throw new RuntimeException();
        }

        LOGGER.info("Loaded {} modifiers", modifierComponents.size());
        ModifierHandler.Global.init(modifierComponents, InjectionStore.of());

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

    public static @NotNull ZombiesDatabase getDatabase() {
        return FeatureUtils.check(database);
    }

    private record PreloadedMap(@NotNull List<String> instancePath,
        @NotNull Point spawn,
        int chunkLoadRange) {

    }

}
