package org.phantazm.zombies.equipment.gun.shoot.blockiteration;

import com.github.steanky.element.core.annotation.*;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.shoot.wallshooting.WallshootingChecker;

import java.util.Objects;
import java.util.Set;

/**
 * A {@link BlockIteration} method that employs a technique called "wallshooting". If a shot intersects a block whose
 * bounding box is not a cube, the shot will pass through all future blocks until it reaches an endpoint. This allows
 * players to shoot through walls by shooting into blocks like slabs. However, if this gun does not pass directly
 * through a non-cube block, it will act the same as a {@link BasicBlockIteration}.
 */
@Model("zombies.gun.block_iteration.wallshot")
@Cache(false)
public class WallshootingBlockIteration implements BlockIteration {

    private final Data data;

    private final WallshootingChecker wallshootingChecker;

    @FactoryMethod
    public WallshootingBlockIteration(@NotNull Data data,
        @NotNull @Child("wallshooting_checker") WallshootingChecker wallshootingChecker) {
        this.data = Objects.requireNonNull(data);
        this.wallshootingChecker = Objects.requireNonNull(wallshootingChecker);
    }

    @Override
    public @NotNull Context createContext() {
        return new Context() {

            private boolean wallshot = false;

            @Override
            public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block) {
                if (wallshootingChecker.canWallshoot()) {
                    return !wallshot;
                }

                return true;
            }

            @SuppressWarnings("UnstableApiUsage")
            @Override
            public boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block) {
                Shape blockShape = block.registry().collisionShape();
                if (wallshootingChecker.canWallshoot() && ((!blockShape.isFullBlock() && !blockShape.isEmpty()) ||
                                                               data.passableBlocks().contains(block.key()))) {
                    wallshot = true;
                    return false;
                }

                return !blockShape.isEmpty();
            }

        };
    }

    @DataObject
    public record Data(@NotNull @ChildPath("wallshooting_checker") String wallshootingChecker,
        @NotNull Set<Key> passableBlocks) {
    }
}
