package org.phantazm.zombies.autosplits.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.server.RoundStartPacket;

import java.util.Objects;

public record RoundStartPacketWrapper(@NotNull RoundStartPacket packet) implements FabricPacket {

    public static final PacketType<RoundStartPacketWrapper> TYPE =
            FabricPacketUtils.createPacketType(RoundStartPacket.ID, RoundStartPacket::read,
                    RoundStartPacketWrapper::new);

    public RoundStartPacketWrapper {
        Objects.requireNonNull(packet, "packet");
    }

    @Override
    public void write(PacketByteBuf buf) {
        packet.write(new PacketByteBufDataWriter());
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
