package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

public interface Agent extends Vec3I {
    void setPosition(@NotNull Vec3I position);

    @NotNull Space getSpace();

    @NotNull NodeExplorer getNodeExplorer();

    @NotNull HeuristicCalculator getHeuristicCalculator();
}