package com.github.phantazmnetwork.api;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.commons.vector.Vec3IBase;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.instance.PreBlockChangeEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerChunkLoadEvent;
import net.minestom.server.event.player.PrePlayerStartDiggingEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.listener.PlayerDiggingListener;
import net.minestom.server.network.PacketProcessor;
import net.minestom.server.network.packet.client.play.ClientPlayerDiggingPacket;
import net.minestom.server.network.packet.server.play.BlockChangePacket;
import net.minestom.server.utils.chunk.ChunkUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Objects;

/**
 * Supports instance-wide client blocks.
 */
public class InstanceClientBlockHandler implements ClientBlockHandler {
    private final Reference<Instance> instance;
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

    /**
     * <p>Constructs a new instance of this class bound to the provided {@link Instance}. This will add a few necessary
     * listeners to the given {@link EventNode}.</p>
     *
     * <p>The lifetime of this object is free to exceed that of the Instance; no strong reference to it is retained.</p>
     * @param instance the instance this handler is bound to
     * @param globalNode the node to add block event listeners to
     */
    public InstanceClientBlockHandler(@NotNull Instance instance, @NotNull EventNode<Event> globalNode) {
        //weakref likely not strictly necessary to prevent memory leaks, but could help instance objects be GC'd sooner
        this.instance = new WeakReference<>(Objects.requireNonNull(instance, "instance"));
        this.clientBlocks = new Long2ObjectOpenHashMap<>();

        globalNode.addListener(EventListener.builder(PlayerChunkLoadEvent.class).filter(event -> event.getInstance() ==
                instance).expireWhen(event -> !instance.isRegistered()).handler(this::onPlayerChunkLoad).build());
        globalNode.addListener(EventListener.builder(PreBlockChangeEvent.class).filter(event -> event.getInstance() ==
                instance).expireWhen(event -> !instance.isRegistered()).handler(this::onPreBlockChange).build());
        globalNode.addListener(EventListener.builder(PlayerBlockBreakEvent.class).filter(event -> event.getInstance() ==
                instance).expireWhen(event -> !instance.isRegistered()).handler(this::onPlayerBlockBreak).build());
        globalNode.addListener(EventListener.builder(PrePlayerStartDiggingEvent.class).filter(event -> event
                .getInstance() == instance).expireWhen(event -> !instance.isRegistered())
                .handler(this::onPrePlayerStartDigging).build());
    }

    @Override
    public void setClientBlock(@NotNull Block type, int x, int y, int z) {
        Instance instance = this.instance.get();
        if(instance == null) {
            return;
        }

        int cx = ChunkUtils.getChunkCoordinate(x);
        int cz = ChunkUtils.getChunkCoordinate(z);
        long index = ChunkUtils.getChunkIndex(cx, cz);

        synchronized (clientBlocks) {
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
                chunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), type));
            }
        }
    }

    @Override
    public void removeClientBlock(int x, int y, int z) {
        Instance instance = this.instance.get();
        if(instance == null) {
            return;
        }

        int cx = ChunkUtils.getChunkCoordinate(x);
        int cz = ChunkUtils.getChunkCoordinate(z);
        long index = ChunkUtils.getChunkIndex(cx, cz);

        synchronized (clientBlocks) {
            ObjectOpenHashSet<PositionedBlock> blocks = clientBlocks.get(index);

            if(blocks != null) {
                //not sus, equals/hashCode is comparable between all Vec3I
                //noinspection SuspiciousMethodCalls
                if(blocks.remove(Vec3I.of(x, y, z))) {
                    Chunk chunk = instance.getChunk(cx, cz);

                    if(chunk != null) {
                        Block serverBlock = chunk.getBlock(x, y, z);

                        //make sure player gets the actual block
                        chunk.sendPacketToViewers(new BlockChangePacket(new Vec(x, y, z), serverBlock));
                    }
                }
            }
        }
    }

    private void onPrePlayerStartDigging(PrePlayerStartDiggingEvent event) {
        Point blockPosition = event.getBlockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientBlocks) {
            ObjectOpenHashSet<PositionedBlock> blocks = clientBlocks.get(index);

            if(blocks != null) {
                PositionedBlock block = blocks.get(VecUtils.toBlockInt(blockPosition));

                if(block != null) {
                    event.setResult(new PlayerDiggingListener.DiggingResult(block.block, false));
                }
            }
        }
    }

    private void onPlayerBlockBreak(PlayerBlockBreakEvent event) {
        Point blockPosition = event.getBlockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientBlocks) {
            ObjectOpenHashSet<PositionedBlock> blocks = clientBlocks.get(index);

            if(blocks != null) {
                //remove the client block; no need to send something else as it will be updated soon
                //noinspection SuspiciousMethodCalls
                blocks.remove(VecUtils.toBlockInt(blockPosition));
            }
        }
    }

    private void onPreBlockChange(PreBlockChangeEvent event) {
        Point blockPosition = event.blockPosition();
        long index = ChunkUtils.getChunkIndex(blockPosition);
        synchronized (clientBlocks) {
            ObjectOpenHashSet<PositionedBlock> blocks = clientBlocks.get(index);

            if(blocks != null) {
                //noinspection SuspiciousMethodCalls
                if(blocks.contains(VecUtils.toBlockInt(blockPosition))) {
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
            ObjectOpenHashSet<PositionedBlock> blocks = clientBlocks.get(index);

            if(blocks != null) {
                Iterator<PositionedBlock> blockIterator = blocks.iterator();
                packets = new BlockChangePacket[blocks.size()];
                for(int i = 0; i < packets.length; i++) {
                    PositionedBlock block = blockIterator.next();
                    packets[i] = new BlockChangePacket(VecUtils.toPoint(block.position), block.block);
                }
            }
        }

        if(packets != null) {
            event.getPlayer().sendPackets(packets);
        }
    }
}
