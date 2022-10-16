package com.github.phantazmnetwork.zombies.equipment.gun.shoot.blockiteration;

import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BlockIteration} method based solely on ray tracing.
 */
@Model("zombies.gun.block_iteration.block")
public class BasicBlockIteration implements BlockIteration {

    @FactoryMethod
    public BasicBlockIteration() {

    }

    @Override
    public @NotNull Context createContext() {
        return new Context() {
            @SuppressWarnings("UnstableApiUsage")
            @Override
            public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Shape shape) {
                return !shape.relativeEnd().isZero();
            }

            @SuppressWarnings("UnstableApiUsage")
            @Override
            public boolean isValidIntersection(@NotNull Vec intersection, @NotNull Shape shape) {
                return true;
            }
        };
    }
}
