package org.phantazm.core.scene2;

/**
 * Abstract implementation of {@link Scene}. Does nothing except maintain shutdown state. Subclasses that override
 * {@link SceneAbstract#shutdown()} should make sure to call {@code super.shutdown()}.
 */
public abstract class SceneAbstract implements Scene {
    private boolean shutdown;

    @Override
    public void shutdown() {
        this.shutdown = true;
    }

    @Override
    public final boolean isShutdown() {
        return shutdown;
    }
}
