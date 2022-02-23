package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.game.scene.SceneFallback;
import com.github.phantazmnetwork.api.instance.InstanceLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicLobbyRouter extends LobbyProviderAbstract {

    private final InstanceManager instanceManager;

    private final InstanceLoader instanceLoader;

    private final String[] lobbyPaths;

    private final SceneFallback fallback;

    private final InstanceConfig instanceConfig;

    public BasicLobbyRouter(int newLobbyThreshold, @NotNull InstanceManager instanceManager,
                            @NotNull InstanceLoader instanceLoader, @NotNull String[] lobbyPaths,
                            @NotNull SceneFallback fallback, @NotNull InstanceConfig instanceConfig) {
        super(newLobbyThreshold);

        this.instanceManager = Objects.requireNonNull(instanceManager, "instanceManager");
        this.instanceLoader = Objects.requireNonNull(instanceLoader, "instanceLoader");
        this.lobbyPaths = Objects.requireNonNull(lobbyPaths, "lobbyPaths");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");
    }

    @Override
    protected @NotNull Lobby createLobby() {
        Instance instance = instanceLoader.loadInstance(instanceManager, lobbyPaths);
        return new Lobby(instance, instanceConfig, fallback);
    }

}
