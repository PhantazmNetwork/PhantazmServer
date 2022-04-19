package com.github.phantazmnetwork.neuron.navigator;

import com.github.phantazmnetwork.neuron.node.Node;
import org.jetbrains.annotations.NotNull;

public interface Controller {
    double getX();

    double getY();

    double getZ();

    void advance(@NotNull Node node);
}
