package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.bindings.minestom.chunk.NeuralChunk;
import com.github.phantazmnetwork.neuron.world.Solid;
import com.github.phantazmnetwork.neuron.world.VoxelSpace;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A {@link VoxelSpace} implementation which uses a Minestom {@link Instance} as a supplier of {@link NeuralChunk}s.
 */
public class InstanceSpace extends VoxelSpace {
    private final Instance instance;

    /**
     * Creates a new instance of this class. The given {@link Instance} may only supply {@link NeuralChunk}s, as these
     * are used to properly locate and construct {@link Solid} objects.
     * @param instance the instance which will supply solids and chunks as needed
     */
    public InstanceSpace(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @Override
    public @Nullable Solid solidAt(int x, int y, int z) {
        Chunk chunk = instance.getChunk(x >> 4, z >> 4);
        if(chunk == null) {
            return null;
        }

        if(chunk instanceof NeuralChunk neuralChunk) {
            return neuralChunk.getSolid(x, y, z);
        }

        throw new IllegalStateException("Instance must return NeuralChunks");
    }
}