package org.phantazm.messaging.serialization;

import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.packet.c2p.MapDataVersionQueryPacket;
import org.phantazm.messaging.packet.c2p.MapDataVersionResponsePacket;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Creates common {@link PacketSerializer}s.
 */
public final class PacketSerializers {

    private static final Map<Byte, Function<DataReader, Packet>> clientToProxyDeserializers;

    static {
        clientToProxyDeserializers =
                Map.of(MapDataVersionQueryPacket.ID, MapDataVersionQueryPacket::read, MapDataVersionResponsePacket.ID,
                        MapDataVersionResponsePacket::read);
    }

    private PacketSerializers() {
        throw new UnsupportedOperationException();
    }

    public static @NotNull PacketSerializer clientToServerSerializer(@NotNull Supplier<DataWriter> writerCreator,
            @NotNull Function<byte[], DataReader> readerCreator) {
        return new PacketSerializer(Collections.emptyMap(), writerCreator, readerCreator);
    }

    public static @NotNull PacketSerializer clientToProxySerializer(@NotNull Supplier<DataWriter> writerCreator,
            @NotNull Function<byte[], DataReader> readerCreator) {
        return new PacketSerializer(clientToProxyDeserializers, writerCreator, readerCreator);
    }

}
