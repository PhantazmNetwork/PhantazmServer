package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.game.scene.SceneProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class LobbyProviderAbstract implements SceneProvider<Lobby> {

    private final List<Lobby> lobbies = new ArrayList<>();

    private final List<Lobby> unmodifiableLobbies = Collections.unmodifiableList(lobbies);

    private final int newLobbyThreshold;

    public LobbyProviderAbstract(int newLobbyThreshold) {
        this.newLobbyThreshold = newLobbyThreshold;
    }

    @Override
    public @NotNull Lobby provideScene() {
        Lobby minimumLobby = null;
        int minimumOnline = Integer.MAX_VALUE;

        for (Lobby lobby : lobbies) {
            int playerCount = lobby.getIngamePlayerCount();

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
