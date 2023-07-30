package org.phantazm.zombies.mapeditor.client.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.proxy.MapDataVersionResponsePacket;

import java.util.Objects;

public record MapDataVersionResponsePacketWrapper(@NotNull MapDataVersionResponsePacket packet) implements FabricPacket {

    public static final PacketType<MapDataVersionResponsePacketWrapper> TYPE =
            FabricPacketUtils.createPacketType(MapDataVersionResponsePacket.ID, MapDataVersionResponsePacket::read,
                    MapDataVersionResponsePacketWrapper::new);

    public MapDataVersionResponsePacketWrapper {
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
