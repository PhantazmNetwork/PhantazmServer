package com.github.phantazmnetwork.zombies.equipment.gun.target.intersectionfinder;

import com.github.phantazmnetwork.commons.Namespaces;
import com.github.phantazmnetwork.core.RayUtils;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * An {@link IntersectionFinder} that uses ray tracing to find intersections.
 */
public class RayTraceIntersectionFinder implements IntersectionFinder {

    /**
     * Creates a {@link ConfigProcessor} for {@link Data}s.
     *
     * @return A {@link ConfigProcessor} for {@link Data}s
     */
    public static @NotNull ConfigProcessor<Data> processor() {
        return ConfigProcessor.emptyProcessor(Data::new);
    }

    @Override
    public @NotNull Optional<Vec> getHitLocation(@NotNull Entity entity, @NotNull Pos start) {
        return RayUtils.rayTrace(entity.getBoundingBox(), entity.getPosition(), start);
    }

    /**
     * Data for a {@link RayTraceIntersectionFinder}.
     */
    public record Data() implements Keyed {

        /**
         * The serial {@link Key} of this {@link Data}.
         */
        public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.intersection_finder.ray_trace");

        @Override
        public @NotNull Key key() {
            return SERIAL_KEY;
        }

    }

}
