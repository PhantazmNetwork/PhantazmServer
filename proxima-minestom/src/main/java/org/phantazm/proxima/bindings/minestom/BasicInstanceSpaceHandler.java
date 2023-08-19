package org.phantazm.proxima.bindings.minestom;

import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.BlockChangeEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicInstanceSpaceHandler implements InstanceSpaceHandler {
    private final InstanceSpace space;

    public BasicInstanceSpaceHandler(@NotNull InstanceSpace space, @NotNull EventNode<InstanceEvent> instanceNode) {
        this.space = Objects.requireNonNull(space);

        instanceNode.addListener(InstanceChunkUnloadEvent.class, this::chunkUnload);
        instanceNode.addListener(BlockChangeEvent.class, this::blockChange);
    }

    private void chunkUnload(InstanceChunkUnloadEvent event) {
        space.clearChunk(event.getChunkX(), event.getChunkZ());
    }

    private void blockChange(BlockChangeEvent event) {
        Block oldBlock = event.getOldBlock();
        Block newBlock = event.getBlock();

        Shape oldShape = oldBlock.registry().collisionShape();
        Shape newShape = newBlock.registry().collisionShape();

        if (oldShape == newShape) {
            return;
        }

        Vec position = event.blockPosition();
        int bx = position.blockX();
        int by = position.blockY();
        int bz = position.blockZ();

        space.updateSolid(bx, by, bz, null);

        if (oldShape.relativeEnd().y() > 1 || newShape.relativeEnd().y() > 1) {
            space.updateSolid(bx, by + 1, bz, null);
        }
    }

    @Override
    public @NotNull InstanceSpace space() {
        return space;
    }

    @Override
    public @NotNull Instance instance() {
        return space.instance();
    }
}
