package org.phantazm.core.instance;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collection;

public record InstanceData(@NotNull Long2ObjectMap<Section[]> chunkData) implements Serializable {
    public static InstanceData of(Instance instance) {
        Collection<Chunk> chunks = instance.getChunks();

        Long2ObjectMap<Section[]> data = new Long2ObjectOpenHashMap<>(chunks.size());
        for (Chunk chunk : chunks) {
            data.put(ChunkUtils.getChunkIndex(chunk), chunk.sectionCopy());
        }

        return new InstanceData(data);
    }
}
