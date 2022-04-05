package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.bindings.minestom.entity.*;
import com.github.phantazmnetwork.neuron.world.BasicSolid;
import com.github.phantazmnetwork.neuron.world.Solid;
import com.github.phantazmnetwork.neuron.world.VoxelSpace;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.Future;

public class InstanceSpace extends VoxelSpace {
    private static final Long2ObjectMap<Solid> SOLID_CACHE = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>(
            512));
    private final Instance instance;


    public InstanceSpace(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @SuppressWarnings({"UnstableApiUsage", "SynchronizationOnLocalVariableOrMethodParameter"})
    @Override
    public @Nullable Solid solidAt(int x, int y, int z) {
        Chunk chunk = instance.getChunk(x >> 4, z >> 4);
        if(chunk == null) {
            return null;
        }

        Block block;
        synchronized (chunk) { //required by Minestom
            block = chunk.getBlock(x, y, z);
        }

        if(!block.isSolid()) {
            return null;
        }

        return SOLID_CACHE.computeIfAbsent(((long) block.stateId() << Integer.SIZE) | block.id(),
                (long ignored) -> {
            Shape shape = block.registry().collisionShape();
            Point start = shape.relativeStart();
            Point end = shape.relativeEnd();
            return new BasicSolid((float) start.x(), (float) start.y(), (float) start.z(), (float) end.x(),
                    (float) end.y(), (float) end.z());
        });
    }
}
