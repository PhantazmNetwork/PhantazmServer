package org.phantazm.proxima.bindings.minestom;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface Spawner {
    @NotNull ProximaEntity spawn(@NotNull Instance instance, @NotNull Point point, @NotNull EntityType entityType,
            @NotNull Pathfinding.Factory factory);
}
