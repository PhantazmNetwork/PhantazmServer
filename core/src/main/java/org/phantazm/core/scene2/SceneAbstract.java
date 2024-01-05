package org.phantazm.core.scene2;

import net.minestom.server.entity.Player;
import net.minestom.server.tag.TagHandler;
import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.player.PlayerView;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplest abstract implementation of {@link Scene}. Maintains shutdown state, as well as optionally shutting down the
 * scene after a predefined timeout period elapses, during which the scene must meet certain conditions that may be
 * defined by subclasses. SceneAbstract instances are <i>identifiable</i>; that is, they implement
 * {@link IdentifiableScene}, as well as {@link JoinToggleableScene} meaning that their joinability can be modified by
 * calling {@link JoinToggleableScene#setJoinable(boolean)}. This cannot be used to make a scene that is undergoing
 * shutdown joinable, however.
 * <p>
 * Subclasses that override {@link SceneAbstract#preShutdown()} should make sure to call {@code super.preShutdown()}.
 * Subclasses that override {@link SceneAbstract#joinable()} should call {@code super.joinable()} and, if false, also
 * return {@code false} themselves. Likewise, implementations must call {@link SceneAbstract#tick(long)} in order for
 * the timeout functionality to work as intended.
 * <p>
 * {@link SceneAbstract#playerTags(UUID)} can be called in order to retrieve a TagHandler instance whose lifetime is the
 * same as this scene; i.e. it will not be cleared until this scene is shut down. This is useful to store persistent
 * game-related data on players.
 */
public abstract class SceneAbstract implements Scene, IdentifiableScene, JoinToggleableScene {
    private final Acquirable<Scene> acquirable = Acquirable.of(this);

    private final int timeout;
    private final UUID identity;

    protected final Set<PlayerView> scenePlayers;
    private final Set<PlayerView> scenePlayersView;
    private final Map<UUID, TagHandler> tagHandlers;

    private int timeoutTicks;
    private boolean shutdown;

    private boolean joinable;

    /**
     * Creates a new SceneAbstract with the specified timeout.
     *
     * @param timeout the timeout, in server ticks (20ths of a second)
     */
    public SceneAbstract(int timeout) {
        this.timeout = timeout;
        this.identity = UUID.randomUUID();
        this.joinable = true;

        this.scenePlayers = new HashSet<>();
        this.scenePlayersView = Collections.unmodifiableSet(this.scenePlayers);
        this.tagHandlers = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull @UnmodifiableView Set<@NotNull PlayerView> playersView() {
        return scenePlayersView;
    }

    @Override
    public void preShutdown() {
        this.shutdown = true;
    }

    @Override
    public boolean joinable() {
        return !isShutdown() && joinable;
    }

    @Override
    public final boolean isShutdown() {
        return shutdown;
    }

    @Override
    public @NotNull Acquirable<? extends Scene> getAcquirable() {
        return acquirable;
    }

    @Override
    public void tick(long time) {
        if (!canTimeout()) {
            timeoutTicks = 0;
            return;
        }

        if (++timeoutTicks == timeout) {
            timeout();
        }
    }

    /**
     * Called every tick to determine if this scene is eligible for timeout. It can be overridden to change the timeout
     * conditions. By default, scenes are eligible for timeout when their player count is 0.
     *
     * @return {@code true} if this scene can time out, {@code false} otherwise
     */
    protected boolean canTimeout() {
        return playerCount() == 0;
    }

    /**
     * Called when the scene times out. By default, this removes the scene from the global {@link SceneManager}, using
     * the default player fallback to handle any players that may already be in the scene.
     */
    protected void timeout() {
        SceneManager.Global.instance().removeScene(this);
    }

    @Override
    public @NotNull UUID identity() {
        return identity;
    }

    @Override
    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final @NotNull TagHandler playerTags(@NotNull UUID playerUuid) {
        return tagHandlers.computeIfAbsent(playerUuid, ignored -> TagHandler.newHandler());
    }

    @Override
    public final @NotNull TagHandler playerTags(@NotNull Player player) {
        return tagHandlers.computeIfAbsent(player.getUuid(), ignored -> TagHandler.newHandler());
    }

    @Override
    public final @NotNull TagHandler playerTags(@NotNull PlayerView playerView) {
        return tagHandlers.computeIfAbsent(playerView.getUUID(), ignored -> TagHandler.newHandler());
    }
}
