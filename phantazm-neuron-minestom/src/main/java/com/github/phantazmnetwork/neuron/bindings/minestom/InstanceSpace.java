package com.github.phantazmnetwork.neuron.bindings.minestom;

import com.github.phantazmnetwork.commons.SingletonIterator;
import com.github.phantazmnetwork.commons.minestom.vector.Vec;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.world.Solid;
import com.github.phantazmnetwork.neuron.world.VoxelSpace;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class InstanceSpace extends VoxelSpace {
    private static class MinestomSolid implements Solid {
        private final Shape shape;
        private final Vec3F min;
        private final Vec3F max;
        private final Iterable<Solid> subSolids;

        private MinestomSolid(Shape shape) {
            this.shape = shape;
            this.min = Vec.toFloat(shape.relativeStart());
            this.max = Vec.toFloat(shape.relativeEnd());

            List<BoundingBox> boundingBoxes = shape.boundingBoxes();
            if(boundingBoxes.size() == 1) {
                this.subSolids = () -> new SingletonIterator<>(this);
            }
            else {
                List<Solid> solids = new ArrayList<>(boundingBoxes.size());
                for (BoundingBox boundingBox : boundingBoxes) {
                    Vec3F min = Vec3F.ofDouble(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ());
                    Vec3F max = Vec3F.ofDouble(boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ());

                    solids.add(new Solid() {
                        private final Iterable<Solid> singleton = () -> new SingletonIterator<>(this);

                        @Override
                        public @NotNull Vec3F getMin() {
                            return min;
                        }

                        @Override
                        public @NotNull Vec3F getMax() {
                            return max;
                        }

                        @Override
                        public @NotNull Iterable<Solid> getComponents() {
                            return singleton;
                        }

                        @Override
                        public boolean overlaps(double x, double y, double z, double width, double height,
                                                double depth) {
                            return boundingBox.intersectBox(new Pos(x, y, z), new BoundingBox(width, height, depth));
                        }
                    });
                }

                this.subSolids = Collections.unmodifiableList(solids);
            }
        }

        @Override
        public @NotNull Vec3F getMin() {
            return min;
        }

        @Override
        public @NotNull Vec3F getMax() {
            return max;
        }

        @Override
        public @NotNull Iterable<Solid> getComponents() {
            return subSolids;
        }

        @Override
        public boolean overlaps(double x, double y, double z, double width, double height, double depth) {
            return shape.intersectBox(new Pos(x, y, z), new BoundingBox(width, height, depth));
        }
    }

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

        //TODO: this naive approach won't work when the block collision exceeds a 1x1x1 cube
        //add a way to split larger collisions into multiple sections
        return SOLID_CACHE.computeIfAbsent(((long) block.stateId() << Integer.SIZE) | block.id(),
                (long ignored) -> new MinestomSolid(block.registry().collisionShape()));
    }
}
