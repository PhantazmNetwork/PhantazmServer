package com.github.phantazmnetwork.api;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.commons.vector.Vec3IBase;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InstanceClientBlockTracker implements ClientBlockTracker {
    private final Instance instance;
    private final Long2ObjectMap<ObjectOpenHashSet<PositionedBlock>> clientBlocks;

    private static class PositionedBlock extends Vec3IBase {
        private Block block;
        private final Vec3I position;

        private PositionedBlock(Block block, Vec3I position) {
            this.block = block;
            this.position = position;
        }

        @Override
        public int getX() {
            return position.getX();
        }

        @Override
        public int getY() {
            return position.getY();
        }

        @Override
        public int getZ() {
            return position.getZ();
        }
    }

    public InstanceClientBlockTracker(@NotNull Instance instance) {
        this.instance = Objects.requireNonNull(instance, "instance");
        this.clientBlocks = new Long2ObjectOpenHashMap<>();
    }

    @Override
    public void setClientBlock(@NotNull Instance instance, @NotNull Block type, int x, int y, int z) {
        int cx = ChunkUtils.getChunkCoordinate(x);
        int cz = ChunkUtils.getChunkCoordinate(z);
        long index = ChunkUtils.getChunkIndex(cx, cz);

        ObjectOpenHashSet<PositionedBlock> positionedBlocks = clientBlocks.get(index);
        if(positionedBlocks == null) {
            clientBlocks.put(index, positionedBlocks = new ObjectOpenHashSet<>(6));
        }

        Vec3I pos = Vec3I.of(x, y, z);
        PositionedBlock block = positionedBlocks.get(pos);
        if(block == null) {
            positionedBlocks.add(new PositionedBlock(type, pos));
        }
        else {
            block.block = type;
        }

        Chunk chunk = instance.getChunk(cx, cz);
        if(chunk != null) {
            chunk.sendPacketToViewers(new BlockChangePacket(new Pos(x, y, z), type));
        }
    }

    @Override
    public void replaceClientBlock(@NotNull Instance instance, @NotNull Block type, int x, int y, int z) {

    }
}
