package com.github.phantazmnetwork.messaging.packet;

import com.github.phantazmnetwork.messaging.proxy.ForwardProtocolVersionPacket;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class PacketSerializers {

    private static final Map<Byte, Function<DataReader, Packet>> proxyDeserializers;

    static {
        proxyDeserializers = new HashMap<>(1);
        proxyDeserializers.put(ForwardProtocolVersionPacket.ID, ForwardProtocolVersionPacket::new);
    }

    public static @NotNull PacketSerializer createProxySerializer(@NotNull Supplier<DataWriter> writerCreator,
                                                                  @NotNull Function<byte[], DataReader> readerCreator) {
        return new PacketSerializer(proxyDeserializers, writerCreator, readerCreator);
    }

    private PacketSerializers() {
        throw new UnsupportedOperationException();
    }

}
