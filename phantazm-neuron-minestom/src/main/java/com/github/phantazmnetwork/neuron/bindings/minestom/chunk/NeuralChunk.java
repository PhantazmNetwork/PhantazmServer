package com.github.phantazmnetwork.neuron.bindings.minestom.chunk;

import com.github.phantazmnetwork.commons.HashStrategies;
import com.github.phantazmnetwork.commons.minestom.vector.VecUtils;
import com.github.phantazmnetwork.commons.vector.Vec3F;
import com.github.phantazmnetwork.neuron.bindings.minestom.solid.SolidProvider;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minestom.server.collision.Shape;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A custom extension of {@link DynamicChunk} that enables fast, asynchronous access to {@link Solid} objects
 * representing collision bounds.
 */
@SuppressWarnings("UnstableApiUsage")
public class NeuralChunk extends DynamicChunk {
    //TODO: test ReadWriteLock depending on how often this map gets queried (it should be mostly reads)
    //(this would need to be done during an actual game, will leave for much later)
    private static final Map<Shape, Solid[]> SPLIT_MAP = new Object2ObjectOpenCustomHashMap<>(HashStrategies
            .identity());

    private final IntSet tallSolids;

    public NeuralChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
        this.tallSolids = new IntOpenHashSet(8);
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        super.setBlock(x, y, z, block);

        int index = ChunkUtils.getBlockIndex(x, y, z);
        synchronized (tallSolids) {
            if(block.registry().collisionShape().relativeEnd().y() > 1) {
                tallSolids.add(index);
            }
            else {
                tallSolids.remove(index);
            }
        }
    }

    public @Nullable Solid getSolid(int x, int y, int z) {
        Block block;
        synchronized (this) {
            block = getBlock(x, y, z, Condition.TYPE);
        }

        if(block == null) {
            return null;
        }

        if(!block.isSolid()) {
            synchronized (tallSolids) {
                if(tallSolids.contains(ChunkUtils.getBlockIndex(x, y, z))) {
                    return getSplitFor(getBlock(x, y, z))[0];
                }

                if(tallSolids.contains(ChunkUtils.getBlockIndex(x, y - 1, z))) {
                    return getSplitFor(getBlock(x, y - 1, z))[1];
                }
            }
        }
        else {
            return SolidProvider.fromShape(block.registry().collisionShape());
        }

        return null;
    }

    private Solid[] getSplitFor(Block block) {
        Shape tallShape = block.registry().collisionShape();

        Solid[] split;
        synchronized (SPLIT_MAP) {
            split = SPLIT_MAP.get(tallShape);
        }

        if(split != null) {
            return split;
        }

        Point start = tallShape.relativeStart();
        Point end = tallShape.relativeEnd();
        Solid[] newSplit = new Solid[] {
                SolidProvider.fromPoints(VecUtils.toFloat(start), Vec3F.ofDouble(end.x(), 1, end.z())),
                SolidProvider.fromPoints(Vec3F.ofDouble(start.x(), 0, start.z()), Vec3F.ofDouble(end.x(),
                        end.y() - 1, end.z()))
        };

        synchronized (SPLIT_MAP) {
            SPLIT_MAP.put(tallShape, newSplit);
        }

        return newSplit;
    }
}
