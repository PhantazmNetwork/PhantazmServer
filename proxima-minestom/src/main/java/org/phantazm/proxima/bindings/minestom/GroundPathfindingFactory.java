package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.Pathfinder;
import com.github.steanky.vector.Vec3I2ObjectMap;
import net.minestom.server.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("proxima.path_settings.ground")
public class GroundPathfindingFactory implements Pathfinding.Factory {
    private final Data data;

    @FactoryMethod
    public GroundPathfindingFactory(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Pathfinding make(@NotNull Pathfinder pathfinder,
            @NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal, @NotNull InstanceSpaceHandler spaceHandler,
            @NotNull EntityType entityType) {
        return new Pathfinding(pathfinder, nodeMapLocal, spaceHandler, entityType) {
            @Override
            protected float jumpHeight() {
                return data.jumpHeight;
            }

            @Override
            protected float fallTolerance() {
                return data.fallTolerance;
            }

            @Override
            protected float stepHeight() {
                return data.stepHeight;
            }
        };
    }

    @DataObject
    public record Data(float jumpHeight, float fallTolerance, float stepHeight) {

    }
}
