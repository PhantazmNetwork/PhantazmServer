package com.github.phantazmnetwork.api;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public interface ClientBlockTracker {
    void setClientBlock(@NotNull Instance instance, @NotNull Block type, int x, int y, int z);

    void replaceClientBlock(@NotNull Instance instance, @NotNull Block type, int x, int y, int z);
}
