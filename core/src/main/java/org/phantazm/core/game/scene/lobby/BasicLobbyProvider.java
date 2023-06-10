package org.phantazm.core.game.scene.lobby;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.config.InstanceConfig;
import org.phantazm.core.game.scene.SceneProviderAbstract;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.instance.InstanceLoader;

import java.util.List;
import java.util.Objects;

/**
 * Basic implementation of a {@link LobbyProviderAbstract}.
 */
public class BasicLobbyProvider extends LobbyProviderAbstract {
    private final InstanceLoader instanceLoader;

    private final List<String> lobbyPaths;

    private final SceneFallback fallback;

    private final InstanceConfig instanceConfig;

    /**
     * Creates a basic implementation of a {@link SceneProviderAbstract}.
     *
     * @param newLobbyThreshold The weighting threshold for {@link Lobby}s. If no {@link Lobby}s are above
     *                          this threshold, a new lobby will be created.
     * @param maximumLobbies    The maximum {@link Lobby}s in the provider.
     * @param instanceLoader    A {@link InstanceLoader} used to load {@link Instance}s
     * @param lobbyPaths        The paths that identify the {@link Lobby} for the {@link InstanceLoader}
     * @param fallback          A {@link SceneFallback} for the created {@link Lobby}s
     * @param instanceConfig    The {@link InstanceConfig} for the {@link Lobby}s
     */
    public BasicLobbyProvider(int maximumLobbies, int newLobbyThreshold, @NotNull InstanceLoader instanceLoader,
            @NotNull List<String> lobbyPaths, @NotNull SceneFallback fallback, @NotNull InstanceConfig instanceConfig) {
        super(maximumLobbies, newLobbyThreshold);

        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.lobbyPaths = List.copyOf(Objects.requireNonNull(lobbyPaths, "lobbyPaths"));
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");
    }

    @Override
    protected @NotNull Lobby createScene(@NotNull LobbyJoinRequest request) {
        Instance instance = instanceLoader.loadInstance(lobbyPaths);
        return new Lobby(instance, instanceConfig, fallback);
    }

    @Override
    protected void cleanupScene(@NotNull Lobby scene) {

    }

}
