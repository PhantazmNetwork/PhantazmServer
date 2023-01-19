package org.phantazm.proxima.bindings.minestom;

import com.github.steanky.proxima.space.Space;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.BlockChangeEvent;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BasicInstanceSpaceHandler implements InstanceSpaceHandler {
    private final InstanceSpace space;
    private final EventNode<Event> root;
    private final EventNode<InstanceEvent> child;

    public BasicInstanceSpaceHandler(@NotNull Instance instance, @NotNull EventNode<Event> root) {
        this.space = new InstanceSpace(instance);
        this.root = Objects.requireNonNull(root, "node");

        this.child = EventNode.event("space_updater",
                EventFilter.from(InstanceEvent.class, Instance.class, InstanceEvent::getInstance),
                event -> event.getInstance() == space.instance());
        this.root.addChild(this.child);
    }

    private void unregisterInstance(InstanceUnregisterEvent event) {
        root.removeChild(child);
    }

    private void chunkUnload(InstanceChunkUnloadEvent event) {
        //space.unloadChunk(event.getChunkX(), event.getChunkZ());
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
        space.updateSolid(position.blockX(), position.blockY(), position.blockY(), null);

        if (oldShape.relativeEnd().y() > 1 || newShape.relativeEnd().y() > 1) {
            space.updateSolid(position.blockX(), position.blockY() + 1, position.blockY(), null);
        }
    }

    @Override
    public @NotNull Space space() {
        return space;
    }
}
