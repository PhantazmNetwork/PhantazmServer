package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.game.scene.SceneProviderAbstract;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.instance.InstanceLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Basic implementation of a {@link LobbyProviderAbstract}.
 */
public class BasicLobbyProvider extends LobbyProviderAbstract {

    private final InstanceManager instanceManager;

    private final InstanceLoader instanceLoader;

    private final String[] lobbyPaths;

    private final SceneFallback fallback;

    private final InstanceConfig instanceConfig;

    /**
     * Creates a basic implementation of a {@link SceneProviderAbstract}.
     * @param newLobbyThreshold The weighting threshold for {@link Lobby}s. If no {@link Lobby}s are above
     *                          this threshold, a new lobby will be created.
     * @param maximumLobbies The maximum {@link Lobby}s in the provider.
     * @param instanceManager An {@link InstanceManager} used to create {@link Instance}
     * @param instanceLoader A {@link InstanceLoader} used to load {@link Instance}s
     * @param lobbyPaths The paths that identify the {@link Lobby} for the {@link InstanceLoader}
     * @param fallback A {@link SceneFallback} for the created {@link Lobby}s
     * @param instanceConfig The {@link InstanceConfig} for the {@link Lobby}s
     */
    public BasicLobbyProvider(int newLobbyThreshold, int maximumLobbies, @NotNull InstanceManager instanceManager,
                              @NotNull InstanceLoader instanceLoader, @NotNull String[] lobbyPaths,
                              @NotNull SceneFallback fallback, @NotNull InstanceConfig instanceConfig) {
        super(newLobbyThreshold, maximumLobbies);

        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.lobbyPaths = Objects.requireNonNull(lobbyPaths, "lobbyPaths");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");
    }

    @Override
    protected @NotNull Lobby createScene(@NotNull LobbyJoinRequest request) {
        Instance instance = instanceLoader.loadInstance(instanceManager, lobbyPaths.clone());
        return new Lobby(instance, instanceConfig, fallback);
    }

}
