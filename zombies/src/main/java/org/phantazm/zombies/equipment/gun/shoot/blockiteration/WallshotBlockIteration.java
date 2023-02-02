package org.phantazm.zombies.equipment.gun.shoot.blockiteration;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

/**
 * A {@link BlockIteration} method that employs a technique called "wallshooting".
 * If a shot intersects a block whose bounding box is not a cube,
 * the shot will pass through all future blocks until it reaches an endpoint.
 * This allows players to shoot through walls by shooting into blocks like slabs.
 * However, if this gun does not pass directly through a non-cube block, it will act the same as
 * a {@link BasicBlockIteration}.
 */
@Model("zombies.gun.block_iteration.wallshot")
@Cache
public class WallshotBlockIteration implements BlockIteration {

    private final Data data;

    @FactoryMethod
    public WallshotBlockIteration(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Context createContext() {
        return new Context() {

            private boolean wallshot = false;

            @Override
            public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block) {
                return !wallshot;
            }

            @SuppressWarnings("UnstableApiUsage")
            @Override
            public boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block) {
                Shape blockShape = block.registry().collisionShape();
                if ((!blockShape.isFullBlock() && !blockShape.isEmpty()) ||
                        data.passableBlocks().contains(block.key())) {
                    wallshot = true;
                    return false;
                }

                return !blockShape.isEmpty();
            }

        };
    }

    @DataObject
    public record Data(@NotNull Collection<Key> passableBlocks) {

        public Data {
            Objects.requireNonNull(passableBlocks, "passableBlocks");
        }

    }

}
