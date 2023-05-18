package org.phantazm.zombies.equipment.gun.shoot.blockiteration;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link BlockIteration} method based solely on ray tracing.
 */
@Model("zombies.gun.block_iteration.block")
@Cache
public class BasicBlockIteration implements BlockIteration {
    private static final Context CONTEXT = new Context() {
        @SuppressWarnings("UnstableApiUsage")
        @Override
        public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block) {
            return !block.registry().collisionShape().relativeEnd().isZero();
        }

        @Override
        public boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block) {
            return true;
        }
    };

    @FactoryMethod
    public BasicBlockIteration() {

    }

    @Override
    public @NotNull Context createContext() {
        return CONTEXT;
    }
}
