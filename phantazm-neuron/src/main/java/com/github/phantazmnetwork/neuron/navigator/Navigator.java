package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

public interface Navigator extends Tickable {
    void setDestination(@NotNull Vec3I destination);
}
