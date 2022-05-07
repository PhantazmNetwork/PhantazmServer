package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.PathEngine;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class TrackingNavigator implements Navigator {
    protected final NavigationTracker navigationTracker;
    protected final PathEngine pathEngine;
    protected final Agent agent;

    public TrackingNavigator(@NotNull NavigationTracker navigationTracker, @NotNull PathEngine pathEngine,
                             @NotNull Agent agent) {
        this.navigationTracker = Objects.requireNonNull(navigationTracker, "pathEngine");
        this.pathEngine = Objects.requireNonNull(pathEngine, "pathEngine");
        this.agent = Objects.requireNonNull(agent, "agent");
    }
}
