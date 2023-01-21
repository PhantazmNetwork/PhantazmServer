package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.Pathfinder;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface Spawner {
    @NotNull ProximaEntity spawn(@NotNull Instance instance, @NotNull Point point, @NotNull EntityType entityType,
            @NotNull PathfindingFactory factory);

    interface PathfindingFactory {
        @NotNull Pathfinding make(@NotNull Pathfinder pathfinder,
                @NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal, @NotNull InstanceSpaceHandler spaceHandler,
                @NotNull EntityType entityType);
    }
}
