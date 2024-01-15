package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.collection.ConfigNode;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.DynamicChunk;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.commons.MonoComponent;
import org.phantazm.commons.FileUtils;
import org.phantazm.core.instance.AnvilFileSystemInstanceLoader;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.npc.NPC;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.lobby.Lobby;
import org.phantazm.core.scene2.lobby.LobbyCreator;
import org.phantazm.loader.Loader;
import org.phantazm.loader.ObjectExtractor;
import org.phantazm.server.config.lobby.LobbyConfig;
import org.phantazm.core.role.RoleStore;
import org.phantazm.server.context.*;
import org.phantazm.zombies.npc.InjectionKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

import static org.phantazm.loader.DataSource.*;

/**
 * Main entrypoint for lobby-related features.
 */
public final class LobbyFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyFeature.class);

    public static final Path LOBBY_INSTANCES_DIRECTORY = Path.of("./lobbies/instances");
    public static final Path LOBBY_CONFIG_DIRECTORY = Path.of("./lobbies/config");

    private static Loader<LobbyEntry> lobbyLoader;

    private LobbyFeature() {
        throw new UnsupportedOperationException();
    }

    public record LobbyEntry(@NotNull LobbyConfig lobbyConfig,
        @NotNull SceneCreator<Lobby> sceneCreator) {
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            if (obj instanceof LobbyEntry other) {
                return lobbyConfig.name().equals(other.lobbyConfig.name());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return lobbyConfig.name().hashCode();
        }
    }

    static void initialize(@NotNull DatabaseContext databaseContext,
        @NotNull EthyleneContext ethyleneContext, @NotNull DataLoadingContext dataLoadingContext,
        @NotNull PlayerContext playerContext, @NotNull ZombiesContext zombiesContext) {
        MappingProcessorSource mappingProcessorSource = ethyleneContext.mappingProcessorSource();
        RoleStore roleStore = playerContext.roles();
        Executor executor = databaseContext.databaseExecutor();
        ConfigCodec codec = ethyleneContext.yamlCodec();
        ContextManager contextManager = dataLoadingContext.contextManager();

        FileUtils.ensureDirectories(LOBBY_INSTANCES_DIRECTORY, LOBBY_CONFIG_DIRECTORY);

        ConfigProcessor<LobbyConfig> lobbyConfigProcessor = mappingProcessorSource
            .processorFor(Token.ofClass(LobbyConfig.class));

        InstanceLoader instanceLoader = new AnvilFileSystemInstanceLoader(MinecraftServer.getInstanceManager(),
            LOBBY_INSTANCES_DIRECTORY, DynamicChunk::new, executor);

        Function<? super Player, ? extends CompletableFuture<?>> displayNameStyler = (player -> {
            return roleStore.getStylingRole(player.getUuid()).whenComplete((result, error) -> {
                if (error != null) {
                    return;
                }

                result.styleDisplayName(player);
            });
        });

        InjectionStore lobbyStore = InjectionStore.of(InjectionKeys.MODIFIER_LOADER_KEY,
            new InjectionKeys.ModifierHandlerLoader(zombiesContext.modifierHandlerLoader()));

        lobbyLoader = Loader.loader(() -> {
            return composite(path -> {
                return merged(namedList(path, codec, "npcs"),
                    namedSingle(path, codec, "settings.yml", "settings"));
            }, Files.list(LOBBY_CONFIG_DIRECTORY));
        }, ObjectExtractor.extractor(ConfigNode.class, (location, element) -> {
            LobbyConfig lobbyConfig = lobbyConfigProcessor.dataFromElement(element.getNodeOrThrow("settings"));
            List<MonoComponent<NPC>> npcs = contextManager.makeContext(element.getListOrThrow("npcs"))
                .provideCollection(ElementPath.of("."));

            return List.of(ObjectExtractor.entry(lobbyConfig.name(), new LobbyEntry(lobbyConfig,
                new LobbyCreator(instanceLoader, lobbyConfig.lobbyPaths(), lobbyConfig.instanceConfig(),
                    lobbyConfig.lobbyJoinFormat(), List.copyOf(npcs), lobbyConfig.defaultItems(), displayNameStyler,
                    lobbyConfig.maxLobbies(), lobbyConfig.maxPlayers(), lobbyConfig.timeout(), lobbyStore))));
        })).accepting(entries -> {
            CompletableFuture<?>[] futures = new CompletableFuture[entries.size()];

            instanceLoader.clearPreloadedInstances();

            int i = 0;
            for (LobbyEntry entry : entries) {
                LobbyConfig lobbyConfig = entry.lobbyConfig;
                futures[i++] = CompletableFuture.runAsync(() -> {
                    instanceLoader.preload(lobbyConfig.lobbyPaths(), lobbyConfig.instanceConfig().spawnPoint(),
                        lobbyConfig.instanceConfig().chunkLoadDistance());
                });
            }

            CompletableFuture.allOf(futures).join();
        }, "preload").accepting(lobbyEntries -> {
            LOGGER.info("Loaded {} lobbies", lobbyEntries.size());
        });

        lobbyLoader.loadUnchecked();
    }

    public static @NotNull Loader<LobbyEntry> lobbies() {
        return FeatureUtils.check(lobbyLoader);
    }

    public static void reload() throws IOException {
        FeatureUtils.check(lobbyLoader).load();
    }
}
