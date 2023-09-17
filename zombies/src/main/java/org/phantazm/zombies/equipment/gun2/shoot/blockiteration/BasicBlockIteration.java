package org.phantazm.zombies.equipment.gun2.shoot.blockiteration;

import com.github.steanky.element.core.annotation.DataObject;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.Set;

public class BasicBlockIteration implements PlayerComponent<BlockIteration> {

    private final Data data;

    public BasicBlockIteration(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull BlockIteration forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return () -> new Context(data.passableBlocks());
    }

    private static class Context implements BlockIteration.Context {

        private final Set<Key> passableBlocks;

        public Context(Set<Key> passableBlocks) {
            this.passableBlocks = passableBlocks;
        }

        @Override
        public boolean isValidEndpoint(@NotNull Point blockLocation, @NotNull Block block) {
            return !passableBlocks.contains(block.key()) && !block.registry().collisionShape().relativeEnd()
                .isZero();
        }

        @Override
        public boolean acceptRaytracedBlock(@NotNull Vec intersection, @NotNull Block block) {
            return true;
        }
    }

    @DataObject
    public record Data(@NotNull Set<Key> passableBlocks) {

    }

}
