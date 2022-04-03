package com.github.phantazmnetwork.api.game.scene.lobby;

import com.github.phantazmnetwork.api.config.InstanceConfig;
import com.github.phantazmnetwork.api.game.scene.RouteResult;
import com.github.phantazmnetwork.api.game.scene.Scene;
import com.github.phantazmnetwork.api.game.scene.fallback.SceneFallback;
import com.github.phantazmnetwork.api.player.PlayerView;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents a lobby. Most basic scene which contains {@link Player}s.
 */
public class Lobby implements Scene<LobbyJoinRequest> {

    private final Instance instance;

    private final InstanceConfig instanceConfig;

    private final SceneFallback fallback;

    private final Map<UUID, PlayerView> players = new HashMap<>();

    private final Map<UUID, PlayerView> unmodifiablePlayers = Collections.unmodifiableMap(players);

    private boolean shutdown = false;

    private boolean joinable = true;

    /**
     * Creates a lobby.
     * @param instance The {@link Instance} that the lobby's players are sent to
     * @param instanceConfig The {@link InstanceConfig} used for the lobby's {@link Instance}
     * @param fallback A fallback for the lobby
     */
    public Lobby(@NotNull Instance instance, @NotNull InstanceConfig instanceConfig, @NotNull SceneFallback fallback) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.instanceConfig = Objects.requireNonNull(instanceConfig, "instanceConfig");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public @NotNull RouteResult join(@NotNull LobbyJoinRequest joinRequest) {
        if (isShutdown()) {
            return new RouteResult(false, Optional.of(Component.text("Lobby is shutdown.")));
        }
        if (!isJoinable()) {
            return new RouteResult(false, Optional.of(Component.text("Lobby is not joinable.")));
        }

        Collection<Pair<PlayerView, Player>> joiners = new ArrayList<>();
        for (PlayerView playerView : joinRequest.getPlayers()) {
            playerView.getPlayer().ifPresent(player -> {
                if (player.getInstance() != instance) {
                    joiners.add(Pair.of(playerView, player));
                }
            });
        }

        if (joiners.isEmpty()) {
            return new RouteResult(false,
                    Optional.of(Component.text("Everybody is already in the lobby.")));
        }

        for (Pair<PlayerView, Player> player : joiners) {
            joinRequest.handleJoin(instance, instanceConfig);
            players.put(player.first().getUUID(), player.first());
        }

        return new RouteResult(true, Optional.empty());
    }

    @Override
    public @NotNull RouteResult leave(@NotNull Iterable<UUID> leavers) {
        boolean anyInside = false;
        for (UUID uuid : leavers) {
            if (players.containsKey(uuid)) {
                anyInside = true;
                break;
            }
        }

        if (!anyInside) {
            return new RouteResult(false,
                    Optional.of(Component.text("None of the players are in the lobby.")));
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
    public int getJoinWeight(@NotNull LobbyJoinRequest request) {
        return -(getIngamePlayerCount() + request.getPlayers().size());
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
    public boolean isJoinable() {
        return joinable;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public void tick(long time) {
        // NO-OP
    }

}
