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
    private final EventNode<InstanceEvent> child;

    public BasicInstanceSpaceHandler(@NotNull Instance instance, @NotNull EventNode<Event> root) {
        this.space = new InstanceSpace(instance);
        Objects.requireNonNull(root, "root");

        this.child = EventNode.event("proxima_cache_synchronize_@" + System.identityHashCode(instance),
                EventFilter.from(InstanceEvent.class, Instance.class, InstanceEvent::getInstance),
                event -> event.getInstance() == space.instance());

        this.child.addListener(InstanceUnregisterEvent.class, this::unregisterInstance);
        this.child.addListener(InstanceChunkUnloadEvent.class, this::chunkUnload);
        this.child.addListener(BlockChangeEvent.class, this::blockChange);

        root.addChild(this.child);
    }

    private void unregisterInstance(InstanceUnregisterEvent event) {
        EventNode<? super InstanceEvent> parent = child.getParent();
        if (parent == null) {
            throw new IllegalStateException("event node " + child.getName() + " was orphaned");
        }

        parent.removeChild(child);
        space.clearCache();
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
    public @NotNull Space space() {
        return space;
    }
}
