package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface HeuristicCalculator {
    float compute(@NotNull Vec3I from, @NotNull Vec3I to);
}
