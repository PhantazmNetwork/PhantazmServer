package com.github.phantazmnetwork.neuron.operation;

import org.jetbrains.annotations.NotNull;

public interface NodeProvider {
    Node @NotNull[] getNodes(@NotNull PathContext context, int x, int y, int z);
}
