package com.github.phantazmnetwork.api;

import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public interface ClientBlockTracker {
    void setClientBlock(@NotNull Block type, int x, int y, int z);

    void removeClientBlock(int x, int y, int z);
}
