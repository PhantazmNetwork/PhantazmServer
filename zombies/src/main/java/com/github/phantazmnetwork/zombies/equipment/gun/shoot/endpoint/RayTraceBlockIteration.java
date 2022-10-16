package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.core.RayUtils;
import com.github.phantazmnetwork.core.VecUtils;
import com.github.phantazmnetwork.zombies.game.map.objects.MapObjects;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A {@link BlockIteration} method based solely on ray tracing.
 */
@Model("zombies.gun.block_iteration.ray_trace")
public class RayTraceBlockIteration implements BlockIteration {

    private final Supplier<? extends MapObjects> mapObjects;

    @FactoryMethod
    public RayTraceBlockIteration(@NotNull @Dependency("zombies.dependency.map_object.map_objects")
    Supplier<? extends MapObjects> mapObjects) {
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Optional<Vec> findEnd(@NotNull Instance instance, @NotNull Entity shooter, @NotNull Pos start,
            @NotNull Iterator<Point> it) {
        Point blockLocation = null;
        Block block = null;

        while (it.hasNext()) {
            blockLocation = it.next();
            block = instance.getBlock(blockLocation);

            if (mapObjects.get().windowAt(VecUtils.toBlockInt(blockLocation)).isPresent()) {
                continue;
            }

            Shape shape = block.registry().collisionShape();
            if (!shape.relativeEnd().isZero()) {
                Optional<Vec> intersection = RayUtils.getIntersectionPosition(shape, blockLocation, start);

                if (intersection.isPresent()) {
                    return intersection;
                }
            }
        }

        if (block != null) {
            return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D), blockLocation.add(0.5D, 0D, 0.5D), start);
        }

        return Optional.empty();
    }

}
