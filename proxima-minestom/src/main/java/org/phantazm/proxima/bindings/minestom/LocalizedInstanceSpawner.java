package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.node.Node;
import com.github.steanky.vector.Bounds3I;
import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.EntityType;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

public class LocalizedInstanceSpawner implements Spawner {
    private final ThreadLocal<Vec3I2ObjectMap<Node>> nodeLocal;
    private final InstanceSpaceHandler spaceHandler;

    public LocalizedInstanceSpawner(@NotNull InstanceSpaceHandler spaceHandler, @NotNull Bounds3I searchArea) {
        this.spaceHandler = Objects.requireNonNull(spaceHandler, "spaceHandler");
        this.nodeLocal = ThreadLocal.withInitial(() -> new HashVec3I2ObjectMap<>(searchArea));
    }

    @Override
    public @NotNull ProximaEntity spawn(@NotNull Instance instance, @NotNull Point point,
            @NotNull EntityType entityType, @NotNull PathfindingFactory factory) {
        Pathfinding pathfinding = factory.make(nodeLocal, spaceHandler, entityType);
        return new ProximaEntity(entityType, UUID.randomUUID(), pathfinding);
    }
}
