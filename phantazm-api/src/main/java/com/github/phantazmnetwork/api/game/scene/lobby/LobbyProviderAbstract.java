package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.SceneProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

/**
 * An abstract base for {@link Lobby} {@link SceneProvider}s.
 */
public abstract class LobbyProviderAbstract implements SceneProvider<Lobby> {

    private final List<Lobby> lobbies = new ArrayList<>();

    private final List<Lobby> unmodifiableLobbies = Collections.unmodifiableList(lobbies);

    private final int newLobbyThreshold;

    private final int maximumLobbies;

    /**
     * Creates an abstract lobby provider.
     * @param newLobbyThreshold The weighting threshold for lobbies. If no lobbies are above this threshold, a new lobby
     *                          will be created.
     * @param maximumLobbies The maximum lobbies in the provider.
     */
    public LobbyProviderAbstract(int newLobbyThreshold, int maximumLobbies) {
        this.newLobbyThreshold = newLobbyThreshold;
        this.maximumLobbies = maximumLobbies;
    }

    @Override
    public @NotNull Optional<Lobby> provideScene() {
        if (lobbies.size() >= maximumLobbies) {
            return Optional.empty();
        }

        Lobby maximumLobby = null;
        int maximumWeighting = Integer.MIN_VALUE;

        for (Lobby lobby : lobbies) {
            int joinWeight = lobby.getJoinWeight();

            if (joinWeight > maximumWeighting) {
                maximumLobby = lobby;
                maximumWeighting = joinWeight;
            }
        }

        if (maximumLobby == null || maximumWeighting <= newLobbyThreshold) {
            Lobby lobby = createLobby();
            lobbies.add(lobby);

            return Optional.of(lobby);
        }

        return Optional.of(maximumLobby);
    }

    @Override
    public @UnmodifiableView @NotNull Iterable<Lobby> getScenes() {
        return unmodifiableLobbies;
    }

    @Override
    public void forceShutdown() {
        for (Lobby lobby : lobbies) {
            lobby.forceShutdown();
        }

        lobbies.clear();
    }

    @Override
    public void tick() {
        Iterator<Lobby> iterator = lobbies.iterator();

        while (iterator.hasNext()) {
            Lobby lobby = iterator.next();

            if (lobby.isShutdown()) {
                iterator.remove();
            }
            else {
                lobby.tick();
            }
        }
    }

    /**
     * Creates a {@link Lobby}.
     * @return The new {@link Lobby}
     */
    protected abstract @NotNull Lobby createLobby();

}
