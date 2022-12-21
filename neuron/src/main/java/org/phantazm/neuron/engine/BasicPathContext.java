package org.phantazm.neuron.engine;

import org.jetbrains.annotations.NotNull;
import org.phantazm.neuron.world.Collider;

import java.util.Objects;

/**
 * Basic implementation of {@link PathContext}.
 */
public record BasicPathContext(@NotNull PathEngine getEngine,
                               @NotNull Collider getCollider,
                               @NotNull PathCache getCache) implements PathContext {
    /**
     * Creates a new instance of this record.
     *
     * @param getEngine   the {@link PathEngine} for this context
     * @param getCollider the {@link Collider} for this context
     * @param getCache    the {@link PathCache} for this context
     * @throws NullPointerException if any of the arguments are null
     */
    public BasicPathContext {
        Objects.requireNonNull(getEngine, "getEngine");
        Objects.requireNonNull(getCollider, "getCollider");
        Objects.requireNonNull(getCache, "getCache");
    }
}
