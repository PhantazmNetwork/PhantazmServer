package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.neuron.bindings.minestom.entity.*;
import com.github.phantazmnetwork.neuron.world.BasicSolid;
import com.github.phantazmnetwork.neuron.world.Solid;
import com.github.phantazmnetwork.neuron.world.VoxelSpace;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class InstanceSpace extends VoxelSpace {
    private final Instance instance;

    public InstanceSpace(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @Nullable Solid solidAt(int x, int y, int z) {
        Block block = instance.getBlock(x, y, z);
        if(!block.isSolid()) {
            return null;
        }

        Shape shape = block.registry().collisionShape();
        Point start = shape.relativeStart();
        Point end = shape.relativeEnd();
        return new BasicSolid((float) start.x(), (float) start.y(), (float) start.z(), (float) end.x(), (float) end.y(),
                (float) end.z());
    }
}
