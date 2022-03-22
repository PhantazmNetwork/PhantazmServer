package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.SceneProvider;
import com.github.phantazmnetwork.api.game.scene.SceneProviderAbstract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An abstract {@link Lobby} {@link SceneProvider}.
 */
public abstract class LobbyProviderAbstract extends SceneProviderAbstract<Lobby, LobbyJoinRequest> {

    private final int newLobbyThreshold;

    /**
     * Creates an abstract {@link Lobby} {@link SceneProvider}.
     *
     * @param maximumScenes The maximum number {@link Lobby}s in the provider.
     * @param newLobbyThreshold The weighting threshold for lobbies. If no lobbies are above this threshold,
     * a new {@link Lobby} will be created.
     */
    public LobbyProviderAbstract(int maximumScenes, int newLobbyThreshold) {
        super(maximumScenes);

        this.newLobbyThreshold = newLobbyThreshold;
    }

    @Override
    protected @NotNull Optional<Lobby> chooseScene(@NotNull LobbyJoinRequest request) {
        Lobby maximumLobby = null;
        int maximumWeighting = Integer.MIN_VALUE;

        for (Lobby lobby : getScenes()) {
            int joinWeight = lobby.getJoinWeight(request);

            if (joinWeight > maximumWeighting) {
                maximumLobby = lobby;
                maximumWeighting = joinWeight;
            }
        }

        if (maximumWeighting < newLobbyThreshold) {
            return Optional.empty();
        }

        return Optional.ofNullable(maximumLobby);
    }

}
