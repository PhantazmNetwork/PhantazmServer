package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.PathEngine;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An abstract implementation of {@link Navigator} which holds a {@link NavigationTracker}, {@link PathEngine}, and
 * {@link Agent}.
 */
public abstract class TrackingNavigator implements Navigator {
    protected final NavigationTracker navigationTracker;
    protected final PathEngine pathEngine;
    protected final Agent agent;

    /**
     * Creates a new TrackingNavigator.
     * @param navigationTracker the {@link NavigationTracker} instance used to listen to navigation events
     * @param pathEngine the {@link PathEngine} used to execute path operations
     * @param agent the {@link Agent} this navigator manages
     */
    public TrackingNavigator(@NotNull NavigationTracker navigationTracker, @NotNull PathEngine pathEngine,
                             @NotNull Agent agent) {
        this.navigationTracker = Objects.requireNonNull(navigationTracker, "pathEngine");
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
    }
}
