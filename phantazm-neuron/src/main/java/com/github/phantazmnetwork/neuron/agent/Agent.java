package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Agent {
    @NotNull Vec3I computeStartPosition();

    @NotNull Walker getWalker();

    @NotNull HeuristicCalculator getHeuristicCalculator();

    boolean reachedDestination(int x, int y, int z, int destX, int destY, int destZ);
}