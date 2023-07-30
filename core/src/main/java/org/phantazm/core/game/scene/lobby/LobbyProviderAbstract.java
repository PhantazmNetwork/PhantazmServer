package org.phantazm.core.game.scene.lobby;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneProviderAbstract;
import org.phantazm.core.player.PlayerView;

import java.util.Optional;
import java.util.concurrent.Executor;

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
    public LobbyProviderAbstract(@NotNull Executor executor, int maximumLobbies, int newLobbyThreshold) {
        super(executor, maximumLobbies);

        this.newLobbyThreshold = newLobbyThreshold;
    }

    @Override
    protected @NotNull Optional<Lobby> chooseScene(@NotNull LobbyJoinRequest request) {
        Lobby maximumLobby = null;
        int maximumWeighting = Integer.MAX_VALUE;

        sceneLoop:
        for (Lobby lobby : getScenes()) {
            for (PlayerView playerView : request.getPlayers()) {
                if (lobby.getPlayers().containsKey(playerView.getUUID())) {
                    continue sceneLoop;
                }
            }

            int joinWeight = lobby.getJoinWeight(request);

            if (joinWeight < maximumWeighting) {
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
