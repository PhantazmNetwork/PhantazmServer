package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.SceneProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public abstract class LobbyProviderAbstract implements SceneProvider<Lobby> {

    private final List<Lobby> lobbies = new ArrayList<>();

    private final List<Lobby> unmodifiableLobbies = Collections.unmodifiableList(lobbies);

    private final int newLobbyThreshold;

    private final int maximumLobbies;

    public LobbyProviderAbstract(int newLobbyThreshold, int maximumLobbies) {
        this.newLobbyThreshold = newLobbyThreshold;
        this.maximumLobbies = maximumLobbies;
    }

    @Override
    public @NotNull Optional<Lobby> provideScene() {
        if (lobbies.size() >= maximumLobbies) {
            return Optional.empty();
        }

        Lobby minimumLobby = null;
        int minimumWeighting = Integer.MAX_VALUE;

        for (Lobby lobby : lobbies) {
            int playerCount = lobby.getIngamePlayerCount();

            if (playerCount < minimumWeighting) {
                minimumLobby = lobby;
                minimumWeighting = playerCount;
            }
        }

        if (minimumLobby == null || minimumWeighting >= newLobbyThreshold) {
            Lobby lobby = createLobby();
            lobbies.add(lobby);

            return Optional.of(lobby);
        }

        return Optional.of(minimumLobby);
    }

    @Override
    public @UnmodifiableView @NotNull Iterable<Lobby> listScenes() {
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

    protected abstract @NotNull Lobby createLobby();

}
