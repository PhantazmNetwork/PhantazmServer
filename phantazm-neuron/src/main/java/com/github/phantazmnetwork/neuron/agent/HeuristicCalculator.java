package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.vector.Vec3I;
import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

public interface HeuristicCalculator {
    float compute(@NotNull Space space, @NotNull Vec3I from, @NotNull Vec3I to);
}
