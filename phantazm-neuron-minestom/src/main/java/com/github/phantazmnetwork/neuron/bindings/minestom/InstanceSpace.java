package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.bindings.minestom.chunk.NeuralChunk;
import com.github.phantazmnetwork.neuron.world.Solid;
import com.github.phantazmnetwork.neuron.world.VoxelSpace;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InstanceSpace extends VoxelSpace {
    private final Instance instance;

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