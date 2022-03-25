package com.github.phantazmnetwork.neuron.node;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.neuron.agent.Agent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface EnvironmentCache {
    @Nullable Vec3I forAgent(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ);

    boolean isCached(@NotNull Agent agent, int x, int y, int z, int dX, int dY, int dZ);
}
