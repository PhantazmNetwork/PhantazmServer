package org.phantazm.zombies.equipment.gun2.shoot.blockiteration;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import net.kyori.adventure.key.Key;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.equipment.gun2.shoot.wallshooting.WallshootingChecker;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

public class WallshootingBlockIteration implements PlayerComponent<BlockIteration> {

    private final Data data;

    private final PlayerComponent<WallshootingChecker> wallshootingChecker;

    public WallshootingBlockIteration(@NotNull Data data,
        @NotNull PlayerComponent<WallshootingChecker> wallshootingChecker) {
        this.data = Objects.requireNonNull(data);
        this.wallshootingChecker = Objects.requireNonNull(wallshootingChecker);
    }

    @Override
    public @NotNull BlockIteration forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return () -> new Context(data.passableBlocks(), wallshootingChecker.forPlayer(player, injectionStore));
    }

    private static class Context implements BlockIteration.Context {

        private final Set<Key> passableBlocks;

        private final WallshootingChecker wallshootingChecker;

        private boolean wallshot = false;

        public Context(Set<Key> passableBlocks, WallshootingChecker wallshootingChecker) {
            this.passableBlocks = passableBlocks;
            this.wallshootingChecker = wallshootingChecker;
        }

        @Override
        public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block) {
            if (wallshootingChecker.canWallshoot()) {
                return !wallshot;
            }

            return true;
        }

        @Override
        public boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block) {
            Shape blockShape = block.registry().collisionShape();
            if (wallshootingChecker.canWallshoot() && ((!blockShape.isFullBlock() && !blockShape.isEmpty()) ||
                passableBlocks.contains(block.key()))) {
                wallshot = true;
                return false;
            }

            return !blockShape.isEmpty();
        }
    }

    @DataObject
    public record Data(
        @NotNull @ChildPath("wallshooting_checker") String wallshootingChecker,
        @NotNull Set<Key> passableBlocks) {
    }
}
