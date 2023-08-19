package org.phantazm.zombies.autosplits.packet;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.kyori.adventure.key.Key;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.serialization.DataReader;

import java.util.function.Function;

public class FabricPacketUtils {

    private FabricPacketUtils() {
        throw new UnsupportedOperationException();
    }

    private static Identifier identifierFromKey(Key key) {
        return Identifier.of(key.namespace(), key.value());
    }

    public static void sendPacket(@NotNull PacketSender sender, @NotNull Packet packet) {
        PacketByteBufDataWriter dataWriter = new PacketByteBufDataWriter();
        packet.write(dataWriter);
        sender.sendPacket(identifierFromKey(packet.getId()), dataWriter.getBuf());
    }

    public static <TInnerPacket extends Packet, TWrapperPacket extends FabricPacket> @NotNull PacketType<TWrapperPacket> createPacketType(@NotNull Key key,
        @NotNull Function<DataReader, TInnerPacket> innerCreator,
        @NotNull Function<TInnerPacket, TWrapperPacket> wrapperCreator) {
        return PacketType.create(identifierFromKey(key), buf -> {
            TInnerPacket innerPacket = innerCreator.apply(new PacketByteBufDataReader(buf));
            return wrapperCreator.apply(innerPacket);
        });
    }

}
