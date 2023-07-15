package org.phantazm.zombies.autosplits.messaging;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.packet.PacketHandler;
import org.phantazm.messaging.packet.c2s.RoundStartPacket;
import org.phantazm.messaging.serialization.PacketSerializer;
import org.phantazm.zombies.autosplits.splitter.CompositeSplitter;

import java.util.Objects;

public class PhantazmMessagingHandler extends PacketHandler<PacketSender> {

    private final CompositeSplitter compositeSplitter;

    private final Identifier identifier;

    public PhantazmMessagingHandler(@NotNull PacketSerializer packetSerializer, @NotNull Identifier identifier,
            @NotNull CompositeSplitter compositeSplitter) {
        super(packetSerializer);
        this.identifier = Objects.requireNonNull(identifier, "identifier");
        this.compositeSplitter = Objects.requireNonNull(compositeSplitter, "compositeSplitter");
    }

    @Override
    protected void handlePacket(@NotNull PacketSender packetSender, @NotNull Packet packet) {
        if (packet instanceof RoundStartPacket) {
            compositeSplitter.split();
        }
    }

    @Override
    protected void sendToReceiver(@NotNull PacketSender packetSender, byte @NotNull [] data) {
        packetSender.sendPacket(identifier, new PacketByteBuf(Unpooled.wrappedBuffer(data)));
    }
}
