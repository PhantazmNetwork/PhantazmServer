package org.phantazm.core.instance;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.CompressedProcesser;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

public record InstanceData(@NotNull Long2ObjectMap<ChunkData> chunkData) implements Serializable {
    public record ChunkData(Section[] sections,
        Int2ObjectMap<NBTBlock> entries) implements Serializable {
    }

    public record NBTBlock(int id,
        byte[] nbt) implements Serializable {
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public static @NotNull InstanceData of(@NotNull Instance instance) {
        Collection<Chunk> chunks = instance.getChunks();

        Long2ObjectMap<ChunkData> data = new Long2ObjectOpenHashMap<>(chunks.size());
        for (Chunk chunk : chunks) {
            synchronized (chunk) {
                long index = ChunkUtils.getChunkIndex(chunk);
                Section[] sections = chunk.sectionCopy();
                Int2ObjectMap<Block> entries = chunk.getEntries();

                Int2ObjectMap<NBTBlock> outputMappings = new Int2ObjectOpenHashMap<>(entries.size());
                for (Int2ObjectMap.Entry<Block> entry : entries.int2ObjectEntrySet()) {
                    int key = entry.getIntKey();
                    Block value = entry.getValue();
                    if (!value.hasNbt()) {
                        continue;
                    }

                    NBTCompound nbt = value.nbt();
                    assert nbt != null;

                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    try (NBTWriter writer = new NBTWriter(outputStream, CompressedProcesser.NONE)) {
                        writer.writeNamed("", nbt);
                    } catch (IOException e) {
                        continue;
                    }

                    outputMappings.put(key, new NBTBlock(value.id(), outputStream.toByteArray()));
                }

                data.put(index, new ChunkData(sections, outputMappings));
            }
        }

        return new InstanceData(data);
    }
}
