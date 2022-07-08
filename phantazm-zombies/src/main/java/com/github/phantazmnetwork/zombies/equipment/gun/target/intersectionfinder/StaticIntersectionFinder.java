package com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An {@link IntersectionFinder} that always finds an intersection at the center of an {@link Entity}.
 */
public class StaticIntersectionFinder implements IntersectionFinder {

    /**
     * Data for a {@link StaticIntersectionFinder}.
     */
    public record Data() implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM,"gun.intersection_finder.static");

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
    public @NotNull Optional<Vec> getHitLocation(@NotNull Entity entity, @NotNull Pos start) {
        BoundingBox boundingBox = entity.getBoundingBox();
        double centerX = (boundingBox.minX() + boundingBox.maxX()) / 2;
        double centerY = (boundingBox.minY() + boundingBox.maxY()) / 2;
        double centerZ = (boundingBox.minZ() + boundingBox.maxZ()) / 2;

        return Optional.of(new Vec(centerX, centerY, centerZ));
    }

}
