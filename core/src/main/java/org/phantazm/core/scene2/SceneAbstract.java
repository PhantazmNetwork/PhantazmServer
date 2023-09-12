package org.phantazm.core.scene2;

import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Simplest abstract implementation of {@link Scene}. Does nothing except maintain shutdown state, as well as optionally
 * shutting down the scene after a predefined timeout period elapses, during which the scene must meet certain
 * conditions. Subclasses that override {@link SceneAbstract#preShutdown()} should make sure to call
 * {@code super.preShutdown()}. Subclasses that override {@link SceneAbstract#joinable()} should call
 * {@code super.joinable()} and, if false, also return {@code false} themselves. Likewise, implementations must call
 * {@link SceneAbstract#tick(long)} in order for the timeout functionality to work as intended.
 */
public abstract class SceneAbstract implements Scene {
    private final Acquirable<Scene> acquirable = Acquirable.of(this);
    private final int timeout;

    private int timeoutTicks;
    private boolean shutdown;

    /**
     * Creates a new SceneAbstract with the specified timeout.
     *
     * @param timeout the timeout, in server ticks (20ths of a second)
     */
    public SceneAbstract(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void preShutdown() {
        this.shutdown = true;
    }

    @Override
    public boolean joinable() {
        return !isShutdown();
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
            SceneManager sceneManager = SceneManager.Global.instance();

            sceneManager.removeScene(this, playerViews -> {
                return sceneManager.joinScene(CoreJoinKeys.MAIN_LOBBY, playerViews);
            });
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
}
