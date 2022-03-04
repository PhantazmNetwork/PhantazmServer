package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.Pathfinder;
import org.jetbrains.annotations.NotNull;

public interface PathContext {
    @NotNull Pathfinder getPathfinder();

    @NotNull Agent getAgent();

    @NotNull Vec3I getStartPosition();

    @NotNull Vec3I getDestination();
}
