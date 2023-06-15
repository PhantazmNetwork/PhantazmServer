package org.phantazm.core;

import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3I2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.InstanceChunkUnloadEvent;
import net.minestom.server.event.instance.InstanceUnregisterEvent;
import net.minestom.server.event.instance.PreBlockChangeEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PrePlayerStartDiggingEvent;
import net.minestom.server.event.player.PreSendChunkEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

/**
 * Supports instance-wide client blocks.
 *
 * @see ClientBlockHandlerSource
 */
public class InstanceClientBlockHandler implements ClientBlockHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstanceClientBlockHandler.class);

    private Instance instance;

    private final Long2ObjectMap<Data> clientBlocks;
    private final int chunkFloor;
    private final int chunkHeight;

    private final EventNode<InstanceEvent> childNode;

    /**
     * <p>Constructs a new instance of this class bound to the provided {@link Instance}. This will add a few necessary
     * listeners to the given {@link EventNode}.</p>
     *
     * @param instance   the instance this handler is bound to
     * @param globalNode the node to add block event listeners to
     * @param chunkFloor the minimum y-coordinate of chunks in this instance
     */
    public InstanceClientBlockHandler(@NotNull Instance instance, @NotNull EventNode<Event> globalNode, int chunkFloor,
            int chunkHeight) {
        //weakref necessary as instance field will be captured by the expiration predicate
        this.instance = Objects.requireNonNull(instance, "instance");
        this.clientBlocks = new Long2ObjectOpenHashMap<>();
        this.chunkFloor = chunkFloor;
        this.chunkHeight = chunkHeight;

        UUID uuid = instance.getUniqueId();
        this.childNode = EventNode.event("instance_client_block_handler{" + instance.getUniqueId() + "}",
                EventFilter.from(InstanceEvent.class, Instance.class, InstanceEvent::getInstance),
                instanceEvent -> instanceEvent.getInstance().getUniqueId().equals(uuid));

        childNode.addListener(InstanceUnregisterEvent.class, this::onInstanceUnregister);
        childNode.addListener(PlayerChunkLoadEvent.class, this::onPlayerChunkLoad);
        childNode.addListener(PreBlockChangeEvent.class, this::onPreBlockChange);
        childNode.addListener(PlayerBlockBreakEvent.class, this::onPlayerBlockBreak);
        childNode.addListener(PrePlayerStartDiggingEvent.class, this::onPrePlayerStartDigging);
        childNode.addListener(PreSendChunkEvent.class, this::onPreSendChunk);
        childNode.addListener(InstanceChunkUnloadEvent.class, this::onChunkUnload);

        globalNode.addChild(childNode);
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
        synchronized (clientBlocks) {
            Data data = clientBlocks.get(index);
            if (data == null) {
                data = new Data(new HashVec3I2ObjectMap<>(0, chunkFloor, 0, Chunk.CHUNK_SIZE_X, chunkHeight,
                        Chunk.CHUNK_SIZE_Z), serverChunk.copy(instance, cx, cz));
                clientBlocks.put(index, data);
            }

            PositionedBlock block = data.blocks.get(x, y, z);
            if (block == null) {
                data.blocks.put(x, y, z, new PositionedBlock(type, x, y, z));
            }
            else {
                block.block = type;
            }

            data.chunk.setBlock(x, y, z, type);
            serverChunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), type));
        }
    }

    @Override
    public void clearClientBlocks() {
        Instance instance = this.instance;
        if (instance == null) {
            return;
        }

        synchronized (clientBlocks) {
            for (Long2ObjectMap.Entry<Data> entry : clientBlocks.long2ObjectEntrySet()) {
                long index = entry.getLongKey();
                int x = ChunkUtils.getChunkCoordX(index);
                int z = ChunkUtils.getChunkCoordZ(index);

                Chunk serverChunk = instance.getChunkAt(x, z);
                Vec3I2ObjectMap<PositionedBlock> blocks = entry.getValue().blocks;

                if (serverChunk != null) {
                    for (PositionedBlock block : blocks.values()) {
                        Block serverBlock = serverChunk.getBlock(block.x, block.y, block.z);
                        serverChunk.sendPacketToViewers(
                                new BlockChangePacket(new Vec(block.x, block.y, block.z), serverBlock));
                    }
                }

                blocks.clear();
            }

            clientBlocks.clear();
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
        synchronized (clientBlocks) {
            Data data = clientBlocks.get(index);

            if (data != null) {
                Vec3I2ObjectMap<PositionedBlock> blocks = data.blocks;

                if (blocks.remove(x, y, z) != null) {
                    Chunk chunk = instance.getChunk(cx, cz);

                    if (chunk != null) {
                        Block serverBlock = chunk.getBlock(x, y, z);

                        //make sure player gets the actual block
                        chunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), serverBlock));
                        data.chunk.setBlock(x, y, z, serverBlock);
                    }

                    if (blocks.isEmpty()) {
                        clientBlocks.remove(index);
                    }
                }
            }
        }
    }

    private record Data(Vec3I2ObjectMap<PositionedBlock> blocks, Chunk chunk) {
    }

    private void onPreSendChunk(PreSendChunkEvent event) {
        Chunk chunk = event.chunk();

        int cx = chunk.getChunkX();
        int cz = chunk.getChunkZ();
        long index = ChunkUtils.getChunkIndex(cx, cz);

        synchronized (clientBlocks) {
            Data data = clientBlocks.get(index);

            if (data != null) {
                event.setChunk(data.chunk);
            }
        }
    }

    private void onChunkUnload(InstanceChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();

        int cx = chunk.getChunkX();
        int cz = chunk.getChunkZ();
        long index = ChunkUtils.getChunkIndex(cx, cz);

        synchronized (clientBlocks) {
            clientBlocks.remove(index);
        }
    }

    private void onInstanceUnregister(InstanceUnregisterEvent event) {
        EventNode<? super InstanceEvent> parent = childNode.getParent();
        if (parent != null) {
            parent.removeChild(childNode);
        }
        else {
            LOGGER.warn("Orphaned event node " + childNode.getName());
        }

        this.instance = null;

        synchronized (clientBlocks) {
            this.clientBlocks.clear();
        }
    }

    private void onPrePlayerStartDigging(PrePlayerStartDiggingEvent event) {
        Point blockPosition = event.getBlockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientBlocks) {
            Data data = clientBlocks.get(index);

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
        synchronized (clientBlocks) {
            Data data = clientBlocks.get(index);

            if (data != null) {
                //remove the client block; no need to send something else as it will be updated soon
                if (data.blocks.remove(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ()) !=
                        null && data.blocks.isEmpty()) {
                    clientBlocks.remove(index);
                }
            }
        }
    }

    private void onPreBlockChange(PreBlockChangeEvent event) {
        Point blockPosition = event.blockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientBlocks) {
            Data data = clientBlocks.get(index);

            if (data != null) {
                Vec3I2ObjectMap<PositionedBlock> blocks = data.blocks;

                if (blocks.containsKey(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ())) {
                    //allow the server to update the block, but don't tell the client
                    event.setSyncClient(false);
                }
            }
        }
    }

    private void onPlayerChunkLoad(PlayerChunkLoadEvent event) {
        int cx = event.getChunkX();
        int cz = event.getChunkZ();

        BlockChangePacket[] packets = null;
        long index = ChunkUtils.getChunkIndex(cx, cz);
        synchronized (clientBlocks) {
            Data data = clientBlocks.get(index);

            if (data != null) {
                Vec3I2ObjectMap<PositionedBlock> blocks = data.blocks;
                Iterator<PositionedBlock> blockIterator = blocks.values().iterator();
                packets = new BlockChangePacket[blocks.size()];
                for (int i = 0; i < packets.length; i++) {
                    PositionedBlock block = blockIterator.next();
                    packets[i] = new BlockChangePacket(new Vec(block.x, block.y, block.z), block.block);
                }
            }
        }

        if (packets != null) {
            event.getPlayer().sendPackets(packets);
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
