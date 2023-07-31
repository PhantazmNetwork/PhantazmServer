package org.phantazm.core.game.scene.lobby;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.SceneProvider;
import org.phantazm.core.game.scene.SceneProviderAbstract;
import org.phantazm.core.game.scene.TransferResult;
import org.phantazm.core.player.PlayerView;

import java.util.*;
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
    protected @NotNull Optional<TransferResult> chooseScene(@NotNull LobbyJoinRequest request) {
        Object2IntMap<UUID> weightMap = new Object2IntOpenHashMap<>(getScenes().size());
        List<Lobby> lobbies = new ArrayList<>(getScenes().size());
        sceneLoop:
        for (Lobby lobby : getScenes()) {
            for (PlayerView playerView : request.getPlayers()) {
                if (lobby.getPlayers().containsKey(playerView.getUUID())) {
                    continue sceneLoop;
                }
            }

            weightMap.put(lobby.getUUID(), lobby.getJoinWeight(request));
            lobbies.add(lobby);
        }

        lobbies.sort(Comparator.comparingInt(lobbyA -> weightMap.getInt(lobbyA.getUUID())));
        if (lobbies.isEmpty()) {
            return Optional.empty();
        }

        if (weightMap.getInt(lobbies.get(0).getUUID()) < newLobbyThreshold) {
            return Optional.empty();
        }

        for (Lobby acceptableLobby : lobbies) {
            TransferResult result = acceptableLobby.join(request);
            if (result.executor().isPresent()) {
                return Optional.of(result);
            }
        }

        return Optional.empty();
    }

}
