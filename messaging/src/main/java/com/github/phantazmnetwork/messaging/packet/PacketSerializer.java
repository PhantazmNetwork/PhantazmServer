package com.github.phantazmnetwork.messaging.packet;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class PacketSerializer {

    private final Map<Byte, Function<DataReader, Packet>> packetDeserializerMap;

    private final Supplier<DataWriter> writerCreator;

    private final Function<byte[], DataReader> readerCreator;

    public PacketSerializer(@NotNull Map<Byte, Function<DataReader, Packet>> packetDeserializerMap,
                            @NotNull Supplier<DataWriter> writerCreator,
                            @NotNull Function<byte[], DataReader> readerCreator) {
        this.packetDeserializerMap = Objects.requireNonNull(packetDeserializerMap, "packetDeserializerMap");
        this.writerCreator = Objects.requireNonNull(writerCreator, "writerCreator");
        this.readerCreator = Objects.requireNonNull(readerCreator, "readerCreator");
    }

    public byte[] serializePacket(@NotNull Packet packet) {
        DataWriter dataWriter = writerCreator.get();
        dataWriter.writeByte(packet.getId());
        packet.write(dataWriter);

        return dataWriter.toByteArray();
    }

    public @NotNull Optional<Packet> deserializePacket(byte @NotNull [] bytes) {
        DataReader dataReader = readerCreator.apply(bytes);
        byte id = dataReader.readByte();

        Function<DataReader, Packet> deserializer = packetDeserializerMap.get(id);
        if (deserializer == null) {
            return Optional.empty();
        }

        return Optional.of(deserializer.apply(dataReader));
    }

}
