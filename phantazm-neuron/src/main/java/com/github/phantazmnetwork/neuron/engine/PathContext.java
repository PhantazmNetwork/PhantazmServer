package com.github.phantazmnetwork.neuron.engine;

import com.github.phantazmnetwork.neuron.world.Collider;
import org.jetbrains.annotations.NotNull;
import com.github.phantazmnetwork.neuron.agent.Agent;

/**
 * Specification for what is essentially a data object providing shared functionality that may be needed by many
 * {@link Agent}s.
 * @see BasicPathContext
 */
public interface PathContext {
    /**
     * The {@link PathEngine} to be used by agents under this context.
     * @return the PathEngine instance to be used
     */
    @NotNull PathEngine getEngine();

    /**
     * The {@link Collider} to be used by agents under this context.
     * @return the Collider instance to be used
     */
    @NotNull Collider getCollider();

    /**
     * The {@link PathCache} to be used by agents under this context.
     * @return the PathCache instance to be used
     */
    @NotNull PathCache getCache();
}
