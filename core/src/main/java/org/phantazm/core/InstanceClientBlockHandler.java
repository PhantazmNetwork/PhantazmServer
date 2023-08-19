package org.phantazm.core;

import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3I2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.instance.PreBlockChangeEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PrePlayerStartDiggingEvent;
import net.minestom.server.event.player.PreSendChunkEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.DynamicChunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Supports instance-wide client blocks.
 *
 * @see ClientBlockHandlerSource
 */
public class InstanceClientBlockHandler implements ClientBlockHandler {
    private final Instance instance;

    private final Long2ObjectMap<Data> clientData;

    private final int chunkFloor;
    private final int chunkHeight;

    /**
     * <p>Constructs a new instance of this class bound to the provided {@link Instance}. This will add a few necessary
     * listeners to the given {@link EventNode}.</p>
     *
     * @param instance   the instance this handler is bound to
     * @param chunkFloor the minimum y-coordinate of chunks in this instance
     */
    public InstanceClientBlockHandler(@NotNull Instance instance, int chunkFloor, int chunkHeight,
        @NotNull EventNode<InstanceEvent> instanceNode) {
        this.instance = Objects.requireNonNull(instance);
        this.clientData = new Long2ObjectOpenHashMap<>();
        this.chunkFloor = chunkFloor;
        this.chunkHeight = chunkHeight;

        instanceNode.addListener(PreBlockChangeEvent.class, this::onPreBlockChange);
        instanceNode.addListener(PlayerBlockBreakEvent.class, this::onPlayerBlockBreak);
        instanceNode.addListener(PrePlayerStartDiggingEvent.class, this::onPrePlayerStartDigging);
        instanceNode.addListener(PreSendChunkEvent.class, this::onPreSendChunk);
        instanceNode.addListener(InstanceChunkUnloadEvent.class, this::onChunkUnload);
    }

    @Override
    public void setClientBlock(@NotNull Block type, int x, int y, int z) {
        Instance instance = this.instance;
        if (instance == null) {
            return;
        }

        Chunk serverChunk = instance.getChunkAt(x, z);
        if (serverChunk == null) {
            return;
        }

        int cx = serverChunk.getChunkX();
        int cz = serverChunk.getChunkZ();

        long index = ChunkUtils.getChunkIndex(cx, cz);
        synchronized (clientData) {
            Data data = clientData.get(index);

            if (data == null) {
                data = new Data(new HashVec3I2ObjectMap<>(0, chunkFloor, 0, Chunk.CHUNK_SIZE_X, chunkHeight,
                    Chunk.CHUNK_SIZE_Z));
                clientData.put(index, data);
            }

            PositionedBlock block = data.blocks.get(x, y, z);
            if (block == null) {
                data.blocks.put(x, y, z, new PositionedBlock(type, x, y, z));
            } else {
                block.block = type;
            }

            data.dirty = true;
            serverChunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), type));
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public void clearClientBlocks() {
        Instance instance = this.instance;
        if (instance == null) {
            return;
        }

        synchronized (clientData) {
            for (Long2ObjectMap.Entry<Data> entry : clientData.long2ObjectEntrySet()) {
                long index = entry.getLongKey();
                int x = ChunkUtils.getChunkCoordX(index);
                int z = ChunkUtils.getChunkCoordZ(index);

                Chunk serverChunk = instance.getChunk(x, z);
                if (serverChunk == null) {
                    continue;
                }

                Vec3I2ObjectMap<PositionedBlock> blocks = entry.getValue().blocks;
                synchronized (serverChunk) {
                    for (PositionedBlock block : blocks.values()) {
                        Block serverBlock = serverChunk.getBlock(block.x, block.y, block.z);
                        serverChunk.sendPacketToViewers(
                            new BlockChangePacket(new Vec(block.x, block.y, block.z), serverBlock));
                    }
                }

                blocks.clear();
            }

            clientData.clear();
        }
    }

    @Override
    public void removeClientBlock(int x, int y, int z) {
        Instance instance = this.instance;
        if (instance == null) {
            return;
        }

        int cx = ChunkUtils.getChunkCoordinate(x);
        int cz = ChunkUtils.getChunkCoordinate(z);
        long index = ChunkUtils.getChunkIndex(cx, cz);
        synchronized (clientData) {
            Data data = clientData.get(index);
            if (data == null) {
                return;
            }

            if (data.blocks.remove(x, y, z) != null) {
                Chunk serverChunk = instance.getChunk(cx, cz);

                if (serverChunk == null) {
                    clientData.remove(index);
                    return;
                }

                Block serverBlock = serverChunk.getBlock(x, y, z);

                //make sure player gets the actual block
                serverChunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), serverBlock));

                if (data.blocks.isEmpty()) {
                    clientData.remove(index);
                }

                data.dirty = true;
            }
        }
    }

    private static class Data {
        private final Vec3I2ObjectMap<PositionedBlock> blocks;
        private DynamicChunk chunk;
        private boolean dirty;

        public Data(Vec3I2ObjectMap<PositionedBlock> blocks) {
            this.blocks = blocks;
        }
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private void onPreSendChunk(PreSendChunkEvent event) {
        Instance instance = this.instance;
        if (instance == null) {
            return;
        }

        DynamicChunk serverChunk = event.chunk();

        int cx = serverChunk.getChunkX();
        int cz = serverChunk.getChunkZ();
        long index = ChunkUtils.getChunkIndex(cx, cz);

        synchronized (clientData) {
            Data data = clientData.get(index);

            if (data != null) {
                DynamicChunk copy = data.chunk;

                boolean updateCopyBlocks = false;
                synchronized (serverChunk) {
                    if (copy == null || copy.getLastChangeTime() != serverChunk.getLastChangeTime() || data.dirty) {
                        copy = serverChunk.copy(instance, cx, cz);
                        data.chunk = copy;
                        updateCopyBlocks = true;
                    }
                }

                if (updateCopyBlocks) {
                    for (PositionedBlock positionedBlock : data.blocks.values()) {
                        copy.setBlock(positionedBlock.x, positionedBlock.y, positionedBlock.z, positionedBlock.block);
                    }

                    data.dirty = false;
                }

                event.setChunk(copy);
            }
        }
    }

    private void onChunkUnload(InstanceChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        int cx = chunk.getChunkX();
        int cz = chunk.getChunkZ();
        long index = ChunkUtils.getChunkIndex(cx, cz);

        synchronized (clientData) {
            clientData.remove(index);
        }
    }

    private void onPrePlayerStartDigging(PrePlayerStartDiggingEvent event) {
        Point blockPosition = event.getBlockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientData) {
            Data data = clientData.get(index);

            if (data != null) {
                PositionedBlock block =
                    data.blocks.get(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ());

                if (block != null) {
                    if (event.getResult().success()) {
                        return; //don't change
                    }

                    event.setResult(new PlayerDiggingListener.DiggingResult(block.block, false));
                }
            }
        }
    }

    private void onPlayerBlockBreak(PlayerBlockBreakEvent event) {
        Point blockPosition = event.getBlockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientData) {
            Data data = clientData.get(index);

            if (data != null) {
                //remove the client block; no need to send something else as it will be updated soon
                if (data.blocks.remove(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ()) !=
                        null && data.blocks.isEmpty()) {
                    clientData.remove(index);
                }
            }
        }
    }

    private void onPreBlockChange(PreBlockChangeEvent event) {
        Point blockPosition = event.blockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientData) {
            Data data = clientData.get(index);

            if (data != null) {
                if (data.blocks.containsKey(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ())) {
                    //allow the server to update the block, but don't tell the client
                    event.setSyncClient(false);
                }
            }
        }
    }

    private static class PositionedBlock {
        private final int x;
        private final int y;
        private final int z;
        private Block block;

        private PositionedBlock(Block block, int x, int y, int z) {
            this.block = block;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
