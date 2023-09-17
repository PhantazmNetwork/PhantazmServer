package org.phantazm.core.scene2;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.player.PlayerView;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Further specialization of {@link SceneAbstract} that assumes a single instance devoted to this {@link Scene}. The
 * scene will unregister the instance during shutdown. As with {@link SceneAbstract}, subclasses that override
 * {@link InstanceScene#shutdown()} should make sure to call {@code super.shutdown()}.
 */
public abstract class InstanceScene extends SceneAbstract implements WatchableScene, TablistLocalScene {
    private final Instance instance;

    private final Set<PlayerView> spectators;
    private final Set<PlayerView> spectatorsView;

    public InstanceScene(@NotNull Instance instance, int timeout) {
        super(timeout);
        this.instance = Objects.requireNonNull(instance);

        this.spectators = new HashSet<>();
        this.spectatorsView = Collections.unmodifiableSet(this.spectators);
    }

    @Override
    public void shutdown() {
        MinecraftServer.getInstanceManager().forceUnregisterInstance(instance);
    }

    /**
     * Gets the instance managed by this InstanceScene.
     *
     * @return the instace for this InstanceScene
     */
    public final @NotNull Instance instance() {
        return instance;
    }

    @Override
    public void joinSpectators(@NotNull Set<? extends @NotNull PlayerView> players, boolean ghost) {
        for (PlayerView player : players) {
            if (!super.scenePlayers.add(player)) {
                continue;
            }

            if (this.spectators.add(player)) {
                player.getPlayer().ifPresent(actualPlayer -> {
                    actualPlayer.updateViewableRule(this::hasSpectator);
                    actualPlayer.setGameMode(GameMode.SPECTATOR);

                    joinSpectator(actualPlayer, ghost);
                });
            }
        }
    }

    @Override
    public @NotNull @UnmodifiableView Set<@NotNull PlayerView> spectatorsView() {
        return spectatorsView;
    }

    protected void joinSpectator(@NotNull Player spectator, boolean ghost) {

    }

    @Override
    public boolean sendRemovalPacketToExistingPlayer(@NotNull Player leavingPlayer, @NotNull Player scenePlayer) {
        return !hasSpectator(leavingPlayer) || hasSpectator(scenePlayer);
    }

    @Override
    public boolean sendRemovalPacketToLeavingPlayer(@NotNull Player leavingPlayer, @NotNull Player scenePlayer) {
        if (hasSpectator(leavingPlayer)) {
            //leaving player is a spectator, always remove from their tablist
            return true;
        }

        return !hasSpectator(scenePlayer);
    }

    @Override
    public @NotNull Set<@NotNull PlayerView> leave(@NotNull Set<? extends @NotNull PlayerView> players) {
        Set<PlayerView> leftPlayers = new HashSet<>(players.size());
        for (PlayerView leavingPlayer : players) {
            if (!super.scenePlayers.remove(leavingPlayer)) {
                continue;
            }

            leftPlayers.add(leavingPlayer);
        }

        return leftPlayers;
    }

    @Override
    public void postLeave(@NotNull Set<? extends @NotNull Player> leftPlayers) {
        TablistLocalScene.super.postLeave(leftPlayers);

        for (Player player : leftPlayers) {
            this.spectators.remove(PlayerView.lookup(player.getUuid()));
        }
    }

    protected @NotNull CompletableFuture<?> teleportOrSetInstance(@NotNull Player player, @NotNull Pos pos) {
        if (player.getInstance() == instance()) {
            return player.teleport(pos);
        }

        return player.setInstance(instance(), pos);
    }
}
