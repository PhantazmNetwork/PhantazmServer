package com.github.phantazmnetwork.neuron.agent;

import com.github.phantazmnetwork.neuron.operation.Node;
import com.github.phantazmnetwork.neuron.world.Space;
import org.jetbrains.annotations.NotNull;

public interface NodeExplorer {
    Node @NotNull[] expandNode(@NotNull Node from);
}
