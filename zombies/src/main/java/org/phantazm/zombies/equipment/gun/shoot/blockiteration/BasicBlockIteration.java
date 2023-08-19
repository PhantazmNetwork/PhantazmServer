package org.phantazm.zombies.equipment.gun.shoot.blockiteration;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

/**
 * A {@link BlockIteration} method based solely on ray tracing.
 */
@Model("zombies.gun.block_iteration.block")
@Cache
public class BasicBlockIteration implements BlockIteration {
    private final Data data;

    @FactoryMethod
    public BasicBlockIteration(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull Context createContext() {
        return new Context() {
            @Override
            public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block) {
                return !data.passableBlocks().contains(block.key()) && !block.registry().collisionShape().relativeEnd()
                    .isZero();
            }

            @Override
            public boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block) {
                return true;
            }
        };
    }

    @DataObject
    public record Data(@NotNull Set<Key> passableBlocks) {

    }

}
