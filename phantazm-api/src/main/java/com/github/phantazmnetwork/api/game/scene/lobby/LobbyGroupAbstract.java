package com.github.phantazmnetwork.api.game.scene.lobby;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class LobbyGroupAbstract implements SceneProvider<Lobby> {

    private final List<Lobby> lobbies = new ArrayList<>();

    private final int newLobbyThreshold;

    public LobbyGroupAbstract(int newLobbyThreshold) {
        this.newLobbyThreshold = newLobbyThreshold;
    }

    @Override
    public @NotNull Lobby provideScene() {
        Lobby minimumLobby = null;
        int minimumOnline = Integer.MAX_VALUE;

        for (Lobby lobby : lobbies) {
            int playerCount = lobby.getOnlinePlayerCount();

            if (playerCount < minimumOnline) {
                minimumLobby = lobby;
                minimumOnline = playerCount;
            }
        }

        if (minimumLobby == null || minimumOnline >= newLobbyThreshold) {
            Lobby lobby = createLobby();
            lobbies.add(lobby);

            return lobby;
        }

        return minimumLobby;
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
        lobbies.removeIf(Lobby::isShutdown);
    }

    protected abstract @NotNull Lobby createLobby();

}
