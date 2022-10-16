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
 * A {@link BlockIteration} method that employs a technique called "wallshooting".
 * If a shot intersects a block whose bounding box is not a cube,
 * the shot will pass through all future blocks until it reaches an endpoint.
 * This allows players to shoot through walls by shooting into blocks like slabs.
 * However, if this gun does not pass directly through a non-cube block, it will act the same as
 * a {@link RayTraceBlockIteration}.
 */
@Model("zombies.gun.block_iteration.wallshot")
public class WallshotBlockIteration implements BlockIteration {

    private final Supplier<? extends MapObjects> mapObjects;

    @FactoryMethod
    public WallshotBlockIteration(@NotNull @Dependency("zombies.dependency.map_object.map_objects")
    Supplier<? extends MapObjects> mapObjects) {
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public @NotNull Optional<Vec> findEnd(@NotNull Instance instance, @NotNull Entity shooter, @NotNull Pos start,
            @NotNull Iterator<Point> it) {
        Point blockLocation = null;
        Block block = null;

        boolean wallshot = false;
        while (it.hasNext()) {
            blockLocation = it.next();
            block = instance.getBlock(blockLocation);

            if (wallshot || mapObjects.get().windowAt(VecUtils.toBlockInt(blockLocation)).isPresent()) {
                continue;
            }

            Shape shape = block.registry().collisionShape();
            if (!shape.relativeEnd().isZero()) {
                Optional<Vec> intersection = RayUtils.getIntersectionPosition(shape, blockLocation, start);
                if (intersection.isPresent()) {
                    if (shape.relativeEnd().equals(Vec.ONE)) {
                        return intersection;
                    }

                    wallshot = true;
                }
            }
        }

        if (block != null) {
            return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D), blockLocation.add(0.5D, 0D, 0.5D), start);
        }

        return Optional.empty();
    }

}
