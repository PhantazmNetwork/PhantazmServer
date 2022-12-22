package org.phantazm.core.game.scene.lobby;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneProviderAbstract;

import java.util.Optional;

/**
 * An abstract {@link Lobby} {@link SceneProvider}.
 */
public abstract class LobbyProviderAbstract extends SceneProviderAbstract<Lobby, LobbyJoinRequest> {

    private final int newLobbyThreshold;

    /**
     * Creates an abstract {@link Lobby} {@link SceneProvider}.
     *
     * @param maximumLobbies    The maximum number {@link Lobby}s in the provider.
     * @param newLobbyThreshold The weighting threshold for lobbies. If no lobbies are above this threshold,
     *                          a new {@link Lobby} will be created.
     */
    public LobbyProviderAbstract(int maximumLobbies, int newLobbyThreshold) {
        super(maximumLobbies);

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
