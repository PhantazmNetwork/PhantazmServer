package org.phantazm.core;

import com.github.steanky.vector.HashVec3I2ObjectMap;
import com.github.steanky.vector.Vec3I2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.PreBlockChangeEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PrePlayerStartDiggingEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Supports instance-wide client blocks.
 *
 * @see ClientBlockHandlerSource
 */
public class InstanceClientBlockHandler implements ClientBlockHandler {
    private final Reference<Instance> instance;
    private final Long2ObjectMap<Vec3I2ObjectMap<PositionedBlock>> clientBlocks;
    private final int chunkFloor;
    private final int chunkHeight;

    /**
     * <p>Constructs a new instance of this class bound to the provided {@link Instance}. This will add a few necessary
     * listeners to the given {@link EventNode}.</p>
     *
     * <p>The lifetime of this object is free to exceed that of the Instance; no strong reference to it is retained.</p>
     *
     * @param instance   the instance this handler is bound to
     * @param globalNode the node to add block event listeners to
     */
    public InstanceClientBlockHandler(@NotNull Instance instance, @NotNull EventNode<Event> globalNode, int chunkFloor,
            int chunkHeight) {
        //weakref necessary as instance field will be captured by the expiration predicate
        this.instance = new WeakReference<>(Objects.requireNonNull(instance, "instance"));
        this.clientBlocks = new Long2ObjectOpenHashMap<>();
        this.chunkFloor = chunkFloor;
        this.chunkHeight = chunkHeight;

        Predicate<InstanceEvent> filter = event -> event.getInstance() == this.instance.get();
        Predicate<Object> expire = ignored -> {
            Instance current = this.instance.get();
            return current == null || !current.isRegistered();
        };

        globalNode.addListener(EventListener.builder(PlayerChunkLoadEvent.class).filter(filter).expireWhen(expire)
                .handler(this::onPlayerChunkLoad).build());
        globalNode.addListener(EventListener.builder(PreBlockChangeEvent.class).filter(filter).expireWhen(expire)
                .handler(this::onPreBlockChange).build());
        globalNode.addListener(EventListener.builder(PlayerBlockBreakEvent.class).filter(filter).expireWhen(expire)
                .handler(this::onPlayerBlockBreak).build());
        globalNode.addListener(EventListener.builder(PrePlayerStartDiggingEvent.class).filter(filter).expireWhen(expire)
                .handler(this::onPrePlayerStartDigging).build());
    }

    @Override
    public void setClientBlock(@NotNull Block type, int x, int y, int z) {
        Instance instance = this.instance.get();
        if (instance == null) {
            return;
        }

        int cx = ChunkUtils.getChunkCoordinate(x);
        int cz = ChunkUtils.getChunkCoordinate(z);
        long index = ChunkUtils.getChunkIndex(cx, cz);

        synchronized (clientBlocks) {
            Vec3I2ObjectMap<PositionedBlock> positionedBlocks = clientBlocks.get(index);
            if (positionedBlocks == null) {
                clientBlocks.put(index,
                        positionedBlocks = new HashVec3I2ObjectMap<>(0, chunkFloor, 0, 16, chunkHeight, 16));
            }

            PositionedBlock block = positionedBlocks.get(x, y, z);
            if (block == null) {
                positionedBlocks.put(x, y, z, new PositionedBlock(type, x, y, z));
            }
            else {
                block.block = type;
            }

            Chunk chunk = instance.getChunk(cx, cz);
            if (chunk != null) {
                chunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), type));
            }
        }
    }

    @Override
    public void clearClientBlocks() {
        Instance instance = this.instance.get();
        if (instance == null) {
            return;
        }

        synchronized (clientBlocks) {
            for (Long2ObjectMap.Entry<Vec3I2ObjectMap<PositionedBlock>> entry : clientBlocks.long2ObjectEntrySet()) {
                long index = entry.getLongKey();
                int x = ChunkUtils.getChunkCoordX(index);
                int z = ChunkUtils.getChunkCoordZ(index);

                Chunk chunk = instance.getChunkAt(x, z);
                Vec3I2ObjectMap<PositionedBlock> blocks = entry.getValue();
                if (chunk != null) {
                    for (PositionedBlock block : blocks.values()) {
                        Block serverBlock = chunk.getBlock(block.x, block.y, block.z);
                        chunk.sendPacketToViewers(
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
        Instance instance = this.instance.get();
        if (instance == null) {
            return;
        }

        int cx = ChunkUtils.getChunkCoordinate(x);
        int cz = ChunkUtils.getChunkCoordinate(z);
        long index = ChunkUtils.getChunkIndex(cx, cz);
        synchronized (clientBlocks) {
            Vec3I2ObjectMap<PositionedBlock> blocks = clientBlocks.get(index);

            if (blocks != null) {
                if (blocks.remove(x, y, z) != null) {
                    Chunk chunk = instance.getChunk(cx, cz);

                    if (chunk != null) {
                        Block serverBlock = chunk.getBlock(x, y, z);

                        //make sure player gets the actual block
                        chunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), serverBlock));
                    }

                    if (blocks.isEmpty()) {
                        clientBlocks.remove(index);
                    }
                }
            }
        }
    }

    private void onPrePlayerStartDigging(PrePlayerStartDiggingEvent event) {
        Point blockPosition = event.getBlockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientBlocks) {
            Vec3I2ObjectMap<PositionedBlock> blocks = clientBlocks.get(index);

            if (blocks != null) {
                PositionedBlock block =
                        blocks.get(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ());

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
            Vec3I2ObjectMap<PositionedBlock> blocks = clientBlocks.get(index);

            if (blocks != null) {
                //remove the client block; no need to send something else as it will be updated soon
                if (blocks.remove(blockPosition.blockX(), blockPosition.blockY(), blockPosition.blockZ()) != null &&
                        blocks.isEmpty()) {
                    clientBlocks.remove(index);
                }
            }
        }
    }

    private void onPreBlockChange(PreBlockChangeEvent event) {
        Point blockPosition = event.blockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientBlocks) {
            Vec3I2ObjectMap<PositionedBlock> blocks = clientBlocks.get(index);

            if (blocks != null) {
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
            Vec3I2ObjectMap<PositionedBlock> blocks = clientBlocks.get(index);

            if (blocks != null) {
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
