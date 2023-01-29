package org.phantazm.proxima.bindings.minestom.controller;

import com.github.steanky.proxima.node.Node;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Controller {
    void advance(@NotNull Node current, @NotNull Node target, @Nullable Point exactDestination);

    boolean hasControl();
}
