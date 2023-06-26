package org.phantazm.core.npc;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface EntityTicker {
    void accept(long time, @NotNull Entity entity);
}
