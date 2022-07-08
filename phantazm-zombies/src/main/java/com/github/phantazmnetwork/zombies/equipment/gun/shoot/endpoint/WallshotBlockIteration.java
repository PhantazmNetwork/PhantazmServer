package com.github.phantazmnetwork.zombies.equipment.gun.shoot.endpoint;

import com.github.phantazmnetwork.api.RayUtils;
import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
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
import java.util.Optional;

/**
 * A {@link BlockIteration} method that employs a technique called "wallshooting".
 * If a shot intersects a block whose bounding box is not a cube,
 * the shot will pass through all future blocks until it reaches an endpoint.
 * This allows players to shoot through walls by shooting into blocks like slabs.
 * However, if this gun does not pass directly through a non-cube block, it will act the same as
 * a {@link RayTraceBlockIteration}.
 */
public class WallshotBlockIteration implements BlockIteration {

    /**
     * Data for a {@link WallshotBlockIteration}.
     */
    public record Data() implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.block_iteration.wallshot");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }
    }

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
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

            if (wallshot) {
                instance.setBlock(blockLocation, Block.REDSTONE_BLOCK);
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
            return RayUtils.rayTrace(new BoundingBox(1D, 1D, 1D),
                    blockLocation.add(0.5D, 0D, 0.5D), start);
        }

        return Optional.empty();
    }

}
