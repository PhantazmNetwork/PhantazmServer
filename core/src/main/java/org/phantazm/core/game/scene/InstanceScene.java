package org.phantazm.core.game.scene;

import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.game.scene.fallback.SceneFallback;
import org.phantazm.core.player.PlayerView;

import java.util.*;

/**
 * Basic Scene which corresponds to a single {@link Instance} and {@link SceneFallback} to route players.
 *
 * @param <TRequest>
 */
public abstract class InstanceScene<TRequest extends SceneJoinRequest> implements Scene<TRequest> {
    private final UUID uuid;
    protected final Instance instance;
    protected final SceneFallback fallback;
    protected final Point spawnPoint;
    protected final Set<UUID> ghosts;

    private boolean shutdown = false;

    public InstanceScene(@NotNull UUID uuid, @NotNull Instance instance, @NotNull SceneFallback fallback,
            @NotNull Point spawnPoint) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.instance = Objects.requireNonNull(instance, "instance");
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        this.spawnPoint = Objects.requireNonNull(spawnPoint, "spawnPoint");
        this.ghosts = new HashSet<>();
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
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
        shutdown = true;
        MinecraftServer.getInstanceManager().forceUnregisterInstance(instance);
    }

    @Override
    public @NotNull SceneFallback getFallback() {
        return fallback;
    }

    @Override
    public boolean acceptGhost(@NotNull PlayerView playerView) {
        Optional<Player> playerOptional = playerView.getPlayer();
        if (playerOptional.isEmpty()) {
            return false;
        }

        Player player = playerOptional.get();
        Instance oldInstance;
        if ((oldInstance = player.getInstance()) == instance) {
            return false;
        }

        synchronized (ghosts) {
            ghosts.add(player.getUuid());
            ghosts.removeIf(uuid -> {
                Player ghost = MinecraftServer.getConnectionManager().getPlayer(uuid);
                return ghost == null || ghost.getInstance() != instance;
            });
        }

        player.setInstanceAddCallback(
                () -> Utils.handleInstanceTransfer(oldInstance, instance, player, newInstancePlayer -> true,
                        newInstancePlayer -> ghosts.contains(newInstancePlayer.getUuid())));
        player.setInstance(instance, spawnPoint).whenComplete((ignored, err) -> {
            if (err != null) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        });

        return true;
    }
}
