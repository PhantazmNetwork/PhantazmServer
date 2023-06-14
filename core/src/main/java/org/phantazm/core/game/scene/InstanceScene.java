package org.phantazm.core.game.scene;

import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerView;

import java.util.*;

/**
 * Basic Scene which corresponds to a single {@link Instance} and {@link SceneFallback} to route players.
 *
 * @param <TRequest>
 */
public abstract class InstanceScene<TRequest extends SceneJoinRequest> implements Scene<TRequest> {
    protected final Instance instance;
    protected final SceneFallback fallback;
    protected final Map<UUID, PlayerView> players;
    protected final Map<UUID, PlayerView> unmodifiablePlayers;

    private boolean shutdown = false;

    public InstanceScene(@NotNull Instance instance, @NotNull Map<UUID, PlayerView> players,
            @NotNull SceneFallback fallback) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.players = Objects.requireNonNull(players, "players");
        this.unmodifiablePlayers = Collections.unmodifiableMap(players);
        this.fallback = Objects.requireNonNull(fallback, "fallback");
    }

    @Override
    public @UnmodifiableView @NotNull Map<UUID, PlayerView> getPlayers() {
        return unmodifiablePlayers;
    }

    @Override
    public int getIngamePlayerCount() {
        Wrapper<Integer> count = Wrapper.of(0);
        for (PlayerView playerView : getPlayers().values()) {
            playerView.getPlayer().ifPresent(player -> {
                if (player.getInstance() == instance) {
                    count.apply(val -> val + 1);
                }
            });
        }

        return count.get();
    }

    @Override
    public int getJoinWeight(@NotNull TRequest request) {
        return -(getIngamePlayerCount() + request.getRequestWeight());
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public void shutdown() {
        for (PlayerView player : players.values()) {
            player.getPlayer().ifPresent(unused -> fallback.fallback(player));
        }

        players.clear();
        shutdown = true;
    }

    @Override
    public void tick(long time) {
    }
}
