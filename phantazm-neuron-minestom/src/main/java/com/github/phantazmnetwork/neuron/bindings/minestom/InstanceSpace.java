package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.HashStrategies;
import com.github.phantazmnetwork.commons.pipe.Pipe;
import com.github.phantazmnetwork.commons.minestom.vector.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import com.github.phantazmnetwork.neuron.world.VoxelSpace;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.pathfinding.PFColumnarSpace;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class InstanceSpace extends VoxelSpace {
    private static final Map<Shape, Solid> SHARED_SOLIDS = new Object2ObjectOpenCustomHashMap<>(512,
            HashStrategies.identity());

    private final Instance instance;

    public InstanceSpace(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    @Override
    public @Nullable Solid solidAt(int x, int y, int z) {
        Chunk chunk = instance.getChunk(x >> 4, z >> 4);
        if(chunk == null) {
            return null;
        }

        Block block;
        synchronized (chunk) { //required by Minestom, else an exception will be thrown
            block = chunk.getBlock(x, y, z);
        }

        if(!block.isSolid()) {
            return null;
        }

        return SHARED_SOLIDS.computeIfAbsent(block.registry().collisionShape(), MinestomSolid::new);
    }
}