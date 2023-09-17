package org.phantazm.server;

import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.ethylene.core.ConfigCodec;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.bridge.Configuration;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import com.github.steanky.ethylene.mapper.MappingProcessorSource;
import com.github.steanky.ethylene.mapper.type.Token;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.DynamicChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.commons.BasicComponent;
import org.phantazm.commons.FileUtils;
import org.phantazm.core.instance.AnvilFileSystemInstanceLoader;
import org.phantazm.core.instance.InstanceLoader;
import org.phantazm.core.npc.NPC;
import org.phantazm.core.scene2.SceneCreator;
import org.phantazm.core.scene2.lobby.Lobby;
import org.phantazm.core.scene2.lobby.LobbyCreator;
import org.phantazm.server.config.lobby.LobbyConfig;
import org.phantazm.server.role.RoleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Main entrypoint for lobby-related features.
 */
public final class LobbyFeature {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyFeature.class);

    public static final Path LOBBY_INSTANCES_DIRECTORY = Path.of("./lobbies/instances");
    public static final Path LOBBY_CONFIG_DIRECTORY = Path.of("./lobbies/config");
    public static final Path NPC_DIRECTORY = Path.of("./npcs");

    private static Map<Key, LobbyEntry> lobbies;

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

    static void initialize(@NotNull ContextManager contextManager, @NotNull RoleStore roleStore,
        @NotNull Executor executor, @NotNull MappingProcessorSource mappingProcessorSource, @NotNull ConfigCodec codec) {
        try {
            FileUtils.createDirectories(LOBBY_INSTANCES_DIRECTORY);
            FileUtils.createDirectories(LOBBY_CONFIG_DIRECTORY);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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

        Map<Key, LobbyEntry> map = new HashMap<>();
        PathMatcher npcFileMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.{yml,yaml}");

        List<CompletableFuture<?>> preloadFutures = new ArrayList<>();
        try (Stream<Path> lobbyFolderStream = Files.list(LOBBY_CONFIG_DIRECTORY)) {
            for (Path lobbyFolder : (Iterable<? extends Path>) lobbyFolderStream::iterator) {
                if (!Files.isDirectory(lobbyFolder)) {
                    continue;
                }

                Path settingsPath = lobbyFolder.resolve("settings.yml");
                if (!Files.exists(settingsPath)) {
                    LOGGER.warn("Expected settings.yml file in folder " + lobbyFolder);
                    continue;
                }

                LobbyConfig lobbyConfig = Configuration.read(settingsPath, codec, lobbyConfigProcessor);

                Path npcFolder = lobbyFolder.resolve(NPC_DIRECTORY);
                try (Stream<Path> npcFileStream = Files.list(npcFolder)) {
                    List<BasicComponent<NPC>> components = new ArrayList<>();
                    for (Path npcFile : (Iterable<? extends Path>) npcFileStream::iterator) {
                        if (!npcFileMatcher.matches(npcFile)) {
                            continue;
                        }

                        ConfigElement element = Configuration.read(npcFile, codec);
                        if (!element.isNode()) {
                            LOGGER.warn("Expected " + npcFile + " to contain a top-level node");
                            continue;
                        }

                        BasicComponent<NPC> npcComponent = contextManager.makeContext(element.asNode()).provide(elementException -> {
                            LOGGER.warn("Exception when loading NPC file " + npcFile, elementException);
                            throw elementException;
                        }, () -> null);
                        components.add(npcComponent);
                    }

                    if (map.putIfAbsent(lobbyConfig.name(),
                        new LobbyEntry(lobbyConfig, new LobbyCreator(instanceLoader, lobbyConfig.lobbyPaths(),
                            lobbyConfig.instanceConfig(), lobbyConfig.lobbyJoinFormat(), List.copyOf(components),
                            lobbyConfig.defaultItems(), displayNameStyler, lobbyConfig.maxLobbies(),
                            lobbyConfig.maxPlayers(), lobbyConfig.timeout()))) != null) {
                        throw new RuntimeException("Duplicate lobby named " + lobbyConfig.name());
                    }

                    preloadFutures.add(CompletableFuture.runAsync(() -> {
                        instanceLoader.preload(lobbyConfig.lobbyPaths(), lobbyConfig.instanceConfig().spawnPoint(),
                            lobbyConfig.instanceConfig().chunkLoadDistance());
                    }, executor));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        lobbies = Map.copyOf(map);

        CompletableFuture.allOf(preloadFutures.toArray(CompletableFuture[]::new)).join();
    }

    public static @NotNull @Unmodifiable Map<Key, LobbyEntry> lobbies() {
        return FeatureUtils.check(lobbies);
    }
}
