package org.phantazm.velocity.packet;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.packet.proxy.MapDataVersionResponsePacket;
import org.phantazm.messaging.serialization.DataWriter;
import org.phantazm.zombies.map.MapSettingsInfo;

public class VelocityPacketUtils {

    public static byte @NotNull[] serialize(@NotNull Packet packet) {
        DataWriter dataWriter = new ByteArrayOutputDataWriter();
        packet.write(dataWriter);
        return dataWriter.toByteArray();
    }

    public static void sendPacket(@NotNull Player player, @NotNull Packet packet) {
        byte[] data = VelocityPacketUtils.serialize(new MapDataVersionResponsePacket(MapSettingsInfo.MAP_DATA_VERSION));
        player.sendPluginMessage(MinecraftChannelIdentifier.from(packet.getId()), data);
    }

}
