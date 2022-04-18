package com.github.phantazmnetwork.neuron.bindings.minestom.chunk;

import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A custom extension of {@link DynamicChunk} that enables fast, asynchronous access to {@link Solid} objects
 * representing collision bounds.
 */
@SuppressWarnings("UnstableApiUsage")
public class NeuralChunk extends DynamicChunk {
    private final Int2ObjectMap<Solid> specialSolids;

    public NeuralChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
        this.specialSolids = new Int2ObjectOpenHashMap<>();
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        super.setBlock(x, y, z, block);

        if(block.isSolid()) {
            Shape shape = block.registry().collisionShape();
            Point end = shape.relativeEnd();
            if(end.y() > 1) {

            }
        }
    }

    public @Nullable Solid getSolid(int x, int y, int z) {
        return null;
    }
}
