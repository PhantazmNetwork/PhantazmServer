package org.phantazm.proxima.bindings.minestom.controller;

import com.github.steanky.proxima.node.Node;
import org.jetbrains.annotations.NotNull;

public interface Controller {
    void advance(@NotNull Node current, @NotNull Node target, long time);

    boolean hasControl();
}
