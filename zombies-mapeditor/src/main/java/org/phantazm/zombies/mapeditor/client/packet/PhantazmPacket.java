package org.phantazm.zombies.mapeditor.client.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.messaging.MessageChannels;

public record PhantazmPacket(byte @NotNull [] data) implements FabricPacket {

    public static final PacketType<PhantazmPacket> TYPE =
            PacketType.create(new Identifier(Namespaces.PHANTAZM, MessageChannels.CLIENT_TO_SERVER),
                    PhantazmPacket::new);

    public PhantazmPacket(PacketByteBuf buf) {
        this(buf.getWrittenBytes());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBytes(data);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }

}
