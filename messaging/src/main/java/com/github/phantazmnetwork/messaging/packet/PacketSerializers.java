package com.github.phantazmnetwork.messaging.packet;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Creates common {@link PacketSerializer}s.
 */
public final class PacketSerializers {

    private static final Map<Byte, Function<DataReader, Packet>> proxyDeserializers;

    static {
        proxyDeserializers = Collections.emptyMap();
    }

    private PacketSerializers() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a {@link PacketSerializer} for proxy packets.
     *
     * @param writerCreator A creator for the {@link PacketSerializer}'s {@link DataWriter}s
     * @param readerCreator A creator for the {@link PacketSerializer}'s {@link DataReader}s
     * @return A new {@link PacketSerializer}
     */
    public static @NotNull PacketSerializer createProxySerializer(@NotNull Supplier<DataWriter> writerCreator,
            @NotNull Function<byte[], DataReader> readerCreator) {
        return new PacketSerializer(proxyDeserializers, writerCreator, readerCreator);
    }

}
