package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface Navigator extends Tickable {
    void setDestination(@Nullable Supplier<Vec3I> destination);
}
