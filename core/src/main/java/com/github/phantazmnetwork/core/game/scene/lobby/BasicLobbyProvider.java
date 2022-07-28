package com.github.phantazmnetwork.core.game.scene.lobby;

import com.github.phantazmnetwork.core.config.InstanceConfig;
import com.github.phantazmnetwork.core.game.scene.SceneProviderAbstract;
import com.github.phantazmnetwork.core.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.core.instance.InstanceLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Phaser;

/**
 * Basic implementation of a {@link LobbyProviderAbstract}.
 */
public class BasicLobbyProvider extends LobbyProviderAbstract {

    private final InstanceManager instanceManager;

    private final InstanceLoader instanceLoader;

    private final List<String> lobbyPaths;

    private final SceneFallback fallback;

    private final InstanceConfig instanceConfig;

    private final int chunkViewDistance;

    /**
     * Creates a basic implementation of a {@link SceneProviderAbstract}.
     *
     * @param newLobbyThreshold The weighting threshold for {@link Lobby}s. If no {@link Lobby}s are above
     *                          this threshold, a new lobby will be created.
     * @param maximumLobbies    The maximum {@link Lobby}s in the provider.
     * @param instanceManager   An {@link InstanceManager} used to create {@link Instance}
     * @param instanceLoader    A {@link InstanceLoader} used to load {@link Instance}s
     * @param lobbyPaths        The paths that identify the {@link Lobby} for the {@link InstanceLoader}
     * @param fallback          A {@link SceneFallback} for the created {@link Lobby}s
     * @param instanceConfig    The {@link InstanceConfig} for the {@link Lobby}s
     * @param chunkViewDistance The server's chunk view distance
     */
    public BasicLobbyProvider(int maximumLobbies, int newLobbyThreshold, @NotNull InstanceManager instanceManager,
            @NotNull InstanceLoader instanceLoader, @NotNull List<String> lobbyPaths, @NotNull SceneFallback fallback,
            @NotNull InstanceConfig instanceConfig, int chunkViewDistance) {
        super(maximumLobbies, newLobbyThreshold);

        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.lobbyPaths = Collections.unmodifiableList(Objects.requireNonNull(lobbyPaths, "lobbyPaths"));
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");
        this.chunkViewDistance = chunkViewDistance;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected @NotNull Lobby createScene(@NotNull LobbyJoinRequest request) {
        Instance instance = instanceLoader.loadInstance(instanceManager, lobbyPaths);

        Phaser phaser = new Phaser(1);
        ChunkUtils.forChunksInRange(instanceConfig.spawnPoint(), chunkViewDistance, (chunkX, chunkZ) -> {
            phaser.register();
            instance.loadOptionalChunk(chunkX, chunkZ).whenComplete((chunk, throwable) -> phaser.arriveAndDeregister());
        });

        phaser.arriveAndAwaitAdvance();

        return new Lobby(instance, instanceConfig, fallback);
    }

}
