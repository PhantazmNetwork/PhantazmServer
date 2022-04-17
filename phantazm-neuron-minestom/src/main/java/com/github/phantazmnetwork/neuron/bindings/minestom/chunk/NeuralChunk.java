package com.github.phantazmnetwork.neuron.bindings.minestom.chunk;

import com.github.phantazmnetwork.neuron.bindings.minestom.SolidProvider;
import com.github.phantazmnetwork.neuron.world.Solid;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class NeuralChunk extends DynamicChunk {
    private static class ChunkSource implements SolidSource {
        private final Long2ObjectMap<Solid> solidMap;
        private final Object sync = new Object();

        private ChunkSource() {
            this.solidMap = new Long2ObjectOpenHashMap<>();
        }

        @Override
        public @Nullable Solid get(int x, int y, int z) {
            long key = key(x, y, z);
            synchronized (sync) {
                return solidMap.get(key);
            }
        }

        private void set(int x, int y, int z, Solid solid) {
            long key = key(x, y, z);
            synchronized (sync) {
                solidMap.put(key, solid);
            }
        }

        private void remove(int x, int y, int z) {
            long key = key(x, y, z);
            synchronized (sync) {
                solidMap.remove(key);
            }
        }

        private static long key(int x, int y, int z) {
            return (long) y << 8 | (long) x << 4 | z;
        }
    }

    private final ChunkSource chunkSource;

    public NeuralChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        super(instance, chunkX, chunkZ);
        this.chunkSource = new ChunkSource();
    }

    @Override
    public void setBlock(int x, int y, int z, @NotNull Block block) {
        super.setBlock(x, y, z, block);

        //TODO: handle splitting up of abnormal solids (those larger than 1 in some direction)
        if(block.isSolid()) {
            chunkSource.set(x, y, z, SolidProvider.fromShape(block.registry().collisionShape()));
        }
        else {
            chunkSource.remove(x, y, z);
        }
    }

    public @NotNull SolidSource getSolidSource() {
        return chunkSource;
    }
}
