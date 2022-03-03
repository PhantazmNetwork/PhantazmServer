package com.github.phantazmnetwork.neuron.operation;

import com.github.phantazmnetwork.neuron.agent.Agent;
import com.github.phantazmnetwork.neuron.engine.Destination;
import com.github.phantazmnetwork.neuron.engine.Pathfinder;
import com.github.phantazmnetwork.neuron.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

public interface PathContext {
    @NotNull Space getSpace();

    @NotNull Agent getAgent();

    @NotNull Pathfinder getPathfinder();

    @NotNull Destination getDestination();
}
