package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyParser;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.DualComponent;
import org.phantazm.commons.FileUtils;
import org.phantazm.core.BasicClientBlockHandlerSource;
import org.phantazm.core.ClientBlockHandlerSource;
import org.phantazm.core.VecUtils;
import org.phantazm.core.guild.party.Party;
import org.phantazm.core.instance.AnvilFileSystemInstanceLoader;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.player.IdentitySource;
import org.phantazm.core.sound.SongLoader;
import org.phantazm.loader.DataSource;
import org.phantazm.loader.Loader;
import org.phantazm.loader.ObjectExtractor;
import org.phantazm.mob2.MobCreator;
import org.phantazm.proxima.bindings.minestom.InstanceSpawner;
import org.phantazm.server.config.zombies.ZombiesConfig;
import org.phantazm.server.context.*;
import org.phantazm.stats.zombies.ZombiesStatsDatabase;
import org.phantazm.zombies.map.MapLoader;
import org.phantazm.zombies.command.ZombiesCommand;
import org.phantazm.zombies.corpse.CorpseCreator;
import org.phantazm.zombies.endless.Endless;
import org.phantazm.zombies.map.MapInfo;
import org.phantazm.zombies.mob2.BasicMobSpawnerSource;
import org.phantazm.zombies.mob2.MobSpawnerSource;
import org.phantazm.zombies.modifier.*;
import org.phantazm.zombies.player.BasicZombiesPlayerSource;
import org.phantazm.zombies.powerup.BasicPowerupHandlerSource;
import org.phantazm.zombies.powerup.PowerupData;
import org.phantazm.zombies.powerup.PowerupHandler;
import org.phantazm.zombies.scene2.ZombiesJoiner;
import org.phantazm.zombies.scene2.ZombiesLeaderboardContext;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.scene2.ZombiesSceneCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public final class ZombiesFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesFeature.class);

    public static final Path MAPS_FOLDER = Path.of("./zombies/maps");
    public static final Path POWERUPS_FOLDER = Path.of("./zombies/powerups");
    public static final Path INSTANCES_FOLDER = Path.of("./zombies/instances");
    public static final Path MODIFIERS_FOLDER = Path.of("./zombies/modifiers");

    private static Loader<ZombiesSceneCreator> sceneCreatorLoader;
    private static Loader<ModifierHandler> modifierHandlerLoader;
    private static Loader<PowerupHandler.Source> powerupLoader;

    @SuppressWarnings("unchecked")
    static void initialize(@NotNull ConfigContext configContext, @NotNull EthyleneContext ethyleneContext,
        @NotNull DatabaseAccessContext databaseAccessContext,
        @NotNull DataLoadingContext dataLoadingContext, @NotNull PlayerContext playerContext,
        @NotNull GameContext gameContext) {
        ZombiesConfig zombiesConfig = configContext.zombiesConfig();
        ModifierCommandConfig commandConfig = configContext.modifierCommandConfig();

        KeyParser keyParser = ethyleneContext.keyParser();
        MappingProcessorSource mappingProcessorSource = ethyleneContext.mappingProcessorSource();
        ConfigCodec codec = ethyleneContext.yamlCodec();

        ZombiesStatsDatabase database = databaseAccessContext.zombiesStatsDatabase();

        ContextManager contextManager = dataLoadingContext.contextManager();

        Map<? super UUID, ? extends Party> parties = playerContext.parties();

        Loader<MobCreator> mobCreatorLoader = gameContext.mobCreatorLoaderSupplier().get();
        SongLoader songLoader = gameContext.songLoader();
        Function<? super Instance, ? extends InstanceSpawner.InstanceSettings> instanceSettingsFunction =
            gameContext.instanceSettingsFunction();

        FileUtils.ensureDirectories(MAPS_FOLDER, POWERUPS_FOLDER, INSTANCES_FOLDER, MODIFIERS_FOLDER);

        InstanceLoader instanceLoader =
            new AnvilFileSystemInstanceLoader(MinecraftServer.getInstanceManager(), INSTANCES_FOLDER,
                DynamicChunk::new, ExecutorFeature.getExecutor());

        EventNode<Event> globalEventNode = MinecraftServer.getGlobalEventHandler();
        ClientBlockHandlerSource clientBlockHandlerSource = new BasicClientBlockHandlerSource();

        ConfigProcessor<MapInfo> mapInfoConfigProcessor = mappingProcessorSource
            .processorFor(Token.ofClass(MapInfo.class));

        ConfigProcessor<PowerupData> powerupDataProcessor = mappingProcessorSource
            .processorFor(Token.ofClass(PowerupData.class));

        ConfigProcessor<ModifierData> mapDataConfigProcessor = mappingProcessorSource
            .processorFor(Token.ofClass(ModifierData.class));

        powerupLoader = Loader.loader(() -> {
            return DataSource.directory(POWERUPS_FOLDER, codec, "glob:**.{yml,yaml}");
        }, ObjectExtractor.extractor(ConfigNode.class, (location, node) -> {
            PowerupData powerupData = powerupDataProcessor.dataFromElement(node);
            return List.of(ObjectExtractor.entry(powerupData.id(), powerupData));
        })).accepting(powerups -> {
            LOGGER.info("Loaded {} powerups", powerups.size());
        }).mergingMap(powerupDataMap -> {
            return new BasicPowerupHandlerSource(powerupDataMap, contextManager);
        });

        modifierHandlerLoader = Loader.loader(() -> {
            return DataSource.directory(MODIFIERS_FOLDER, codec, "glob:**.{yml,yaml}");
        }, ObjectExtractor.extractor(ConfigNode.class, (location, node) -> {
            ModifierData modifierData = mapDataConfigProcessor.dataFromElement(node);

            ElementContext context = contextManager.makeContext(modifierData.modifier());
            DualComponent<ZombiesScene, Modifier> modifierComponent = context.provide(DependencyProvider.EMPTY);

            return List.of(ObjectExtractor.entry(modifierData.key(), new ModifierSource(modifierData, modifierComponent)));
        })).accepting(modifierSources -> {
            LOGGER.info("Loaded {} modifiers", modifierSources.size());
        }).mergingMap(ModifierHandler::new);

        sceneCreatorLoader = MapLoader.mapInfoLoader(MAPS_FOLDER, codec, mapInfoConfigProcessor)
            .accepting(maps -> {
                Set<PreloadedMap> instancePaths = new HashSet<>(maps.size());
                for (MapInfo mapInfo : maps) {
                    instancePaths.add(new PreloadedMap(mapInfo.settings().instancePath(),
                        VecUtils.toPoint(mapInfo.settings().origin().add(mapInfo.settings().spawn())),
                        mapInfo.settings().chunkLoadRange()));
                }

                instanceLoader.clearPreloadedInstances();

                CompletableFuture<Void>[] loadFutures = new CompletableFuture[instancePaths.size()];
                int i = 0;
                for (PreloadedMap map : instancePaths) {
                    loadFutures[i++] = CompletableFuture.runAsync(() -> instanceLoader.preload(map.instancePath,
                        map.spawn, map.chunkLoadRange));
                }

                CompletableFuture.allOf(loadFutures).join();
            }, "preload")
            .transforming(mapInfo -> {
                CorpseCreator.Source corpseCreatorSource = mapDependencyProvider -> contextManager
                    .makeContext(mapInfo.corpse()).provide(mapDependencyProvider);

                Endless.Source endlessSource = mapDependencyProvider -> contextManager
                    .makeContext(mapInfo.endless()).provide(mapDependencyProvider);

                MobSpawnerSource mobSpawnerSource = new BasicMobSpawnerSource(mobCreatorLoader);

                ZombiesLeaderboardContext leaderboardContext =
                    new ZombiesLeaderboardContext(ExecutorFeature.getExecutor(),
                        databaseAccessContext.zombiesLeaderboardDatabase(),
                        contextManager.makeContext(mapInfo.leaderboard()).provide());

                ModifierHandler modifierHandler = modifierHandlerLoader.anonymousData().iterator().next();
                PowerupHandler.Source powerupHandlerSource = powerupLoader.anonymousData().iterator().next();

                return new ZombiesSceneCreator(zombiesConfig.maximumScenes(), mapInfo, instanceLoader, keyParser,
                    contextManager, songLoader, database, instanceSettingsFunction, globalEventNode,
                    mobSpawnerSource, clientBlockHandlerSource, powerupHandlerSource, modifierHandler,
                    new BasicZombiesPlayerSource(EquipmentFeature::createEquipmentCreator), corpseCreatorSource,
                    endlessSource, leaderboardContext, playerContext.roles(), IdentitySource.MOJANG);
            }, "creator")
            .accepting(maps -> {
                LOGGER.info("Loaded {} maps", maps.size());
            }).dependingOn(powerupLoader, modifierHandlerLoader);

        sceneCreatorLoader.loadUnchecked();

        ZombiesJoiner joiner = new ZombiesJoiner(sceneCreatorLoader, modifierHandlerLoader);

        MinecraftServer.getCommandManager().register(new ZombiesCommand(joiner, parties, keyParser, sceneCreatorLoader,
            zombiesConfig.joinRatelimit(), database, commandConfig, modifierHandlerLoader));
    }

    private record PreloadedMap(@NotNull List<String> instancePath,
        @NotNull Point spawn,
        int chunkLoadRange) {

    }

    public static @NotNull Loader<ModifierHandler> modifierHandlerLoader() {
        return FeatureUtils.check(modifierHandlerLoader);
    }

    public static void reload() throws IOException {
        FeatureUtils.check(sceneCreatorLoader).load();
    }
}
