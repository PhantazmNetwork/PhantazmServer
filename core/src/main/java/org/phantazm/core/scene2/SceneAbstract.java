package org.phantazm.core.scene2;

import net.minestom.server.thread.Acquirable;
import org.jetbrains.annotations.NotNull;

/**
 * Abstract implementation of {@link Scene}. Does nothing except maintain shutdown state. Subclasses that override
 * {@link SceneAbstract#shutdown()} should make sure to call {@code super.shutdown()}. Subclasses that override
 * {@link SceneAbstract#joinable()} should call {@code super.joinable()} and, if false, also return {@code false}
 * themselves.
 */
public abstract class SceneAbstract implements Scene {
    private boolean shutdown;

    private final Acquirable<Scene> acquirable = Acquirable.of(this);

    @Override
    public void shutdown() {
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
}
