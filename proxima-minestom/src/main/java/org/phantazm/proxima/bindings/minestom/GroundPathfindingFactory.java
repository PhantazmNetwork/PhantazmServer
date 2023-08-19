package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import com.github.steanky.proxima.node.Node;
import com.github.steanky.proxima.path.Pathfinder;
import com.github.steanky.vector.Vec3D;
import com.github.steanky.vector.Vec3I2ObjectMap;
import com.github.steanky.vector.Vec3IBiPredicate;
import net.minestom.server.collision.CollisionUtils;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("proxima.path_settings.ground")
@Cache
public class GroundPathfindingFactory implements Pathfinding.Factory {
    private final Data data;

    @FactoryMethod
    public GroundPathfindingFactory(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Pathfinding make(@NotNull Pathfinder pathfinder,
        @NotNull ThreadLocal<Vec3I2ObjectMap<Node>> nodeMapLocal, @NotNull InstanceSpaceHandler spaceHandler) {
        return new Pathfinding(pathfinder, nodeMapLocal, spaceHandler) {
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

            @Override
            protected @NotNull Vec3IBiPredicate successPredicate() {
                return data.targetDeviation <= 0
                           ? (x1, y1, z1, x2, y2, z2) -> x1 == x2 && y1 == y2 && z1 == z2
                           :(x1, y1, z1, x2, y2, z2) -> {
                    if (Vec3D.distanceSquared(x1 + 0.5, y1, z1 + 0.5, x2 + 0.5, y2, z2 + 0.5) <=
                            data.targetDeviation * data.targetDeviation) {
                        if (!data.lineOfSight) {
                            return true;
                        }

                        Entity self = super.self;
                        if (self == null) {
                            return true;
                        }

                        double eyeHeight = self.getEyeHeight();
                        Instance instance = self.getInstance();
                        Entity target = super.target;
                        if (instance == null || target == null) {
                            return true;
                        }

                        Point end = new Vec(x2 + 0.5, y2 + target.getEyeHeight(), z2 + 0.5);
                        Pos start = new Pos(x1 + 0.5, y1 + eyeHeight, z1 + 0.5).withLookAt(end);
                        return CollisionUtils.isLineOfSightReachingShape(instance, self.getChunk(), start, end,
                            target.getBoundingBox());
                    }

                    return false;
                };
            }

            @Override
            public boolean useSynthetic() {
                return data.targetDeviation <= 0;
            }
        };
    }

    @DataObject
    public record Data(float jumpHeight,
        float fallTolerance,
        float stepHeight,
        double targetDeviation,
        boolean lineOfSight) {
        @Default("targetDeviation")
        public static @NotNull ConfigElement defaultTargetDeviation() {
            return ConfigPrimitive.of(0.0);
        }

        @Default("lineOfSight")
        public static @NotNull ConfigElement defaultLineOfSight() {
            return ConfigPrimitive.of(true);
        }
    }
}
