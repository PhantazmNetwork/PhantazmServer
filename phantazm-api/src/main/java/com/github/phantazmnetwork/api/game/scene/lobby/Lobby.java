package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.game.scene.RouteResult;
import com.github.phantazmnetwork.api.game.scene.Scene;
import com.github.phantazmnetwork.api.game.scene.SceneFallback;
import com.github.phantazmnetwork.api.player.PlayerView;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class Lobby implements Scene<LobbyJoinRequest> {

    private final Instance instance;

    private final InstanceConfig instanceConfig;

    private final SceneFallback fallback;

    private final Map<UUID, PlayerView> players = new HashMap<>();

    private final Map<UUID, PlayerView> unmodifiablePlayers = Collections.unmodifiableMap(players);

    private boolean shutdown = false;

    public Lobby(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig, @NotNull SceneFallback fallback) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public @NotNull RouteResult join(@NotNull LobbyJoinRequest joinRequest) {
        if (shutdown) {
            return new RouteResult(false, Optional.of("Lobby is shutdown."));
        }

        for (LobbyJoiner joiner : joinRequest.players()) {
            joiner.player().getPlayer().ifPresent(player -> {
                if (player.getInstance() != instance) {
                    player.setInstance(instance, instanceConfig.spawnPoint());
                }
            });

            players.put(joiner.player().getUUID(), joiner.player());
        }

        return new RouteResult(true, Optional.empty());
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        for (UUID uuid : leavers) {
            if (!players.containsKey(uuid)) {
                return new RouteResult(false, Optional.of(uuid + " is not in the lobby."));
            }
        }

        for (UUID uuid : leavers) {
            players.remove(uuid);
        }

        return new RouteResult(true, Optional.empty());
    }

    @Override
    public @NotNull Map<UUID, PlayerView> getPlayers() {
        return unmodifiablePlayers;
    }

    @Override
    public int getIngamePlayerCount() {
        int[] count = new int[1];
        for (PlayerView playerView : getPlayers().values()) {
            playerView.getPlayer().ifPresent(player -> {
                if (player.getInstance() == instance) {
                    count[0]++;
                }
            });
        }

        return count[0];
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void forceShutdown() {
        for (PlayerView player : players.values()) {
            player.getPlayer().ifPresent(unused -> fallback.fallback(player));
        }
        players.clear();

        shutdown = true;
    }

    @Override
    public void tick() {
        // NO-OP
    }

}
