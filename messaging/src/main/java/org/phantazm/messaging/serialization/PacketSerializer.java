package org.phantazm.messaging.serialization;

import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Converts packets from byte arrays to {@link Packet}s and vice versa.
 */
public class PacketSerializer {

    private final Map<Byte, Function<DataReader, Packet>> packetDeserializerMap;

    private final Supplier<DataWriter> writerCreator;

    private final Function<byte[], DataReader> readerCreator;

    /**
     * Creates a {@link PacketSerializer}.
     *
     * @param packetDeserializerMap A {@link Map} of byte packet IDs to {@link Packet} creators from a {@link DataReader}
     * @param writerCreator         A creator for serialization {@link DataWriter}s
     * @param readerCreator         A creator for deserialization {@link DataReader}s
     */
    public PacketSerializer(@NotNull Map<Byte, Function<DataReader, Packet>> packetDeserializerMap,
            @NotNull Supplier<DataWriter> writerCreator, @NotNull Function<byte[], DataReader> readerCreator) {
        this.packetDeserializerMap = Objects.requireNonNull(packetDeserializerMap, "packetDeserializerMap");
        this.writerCreator = Objects.requireNonNull(writerCreator, "writerCreator");
        this.readerCreator = Objects.requireNonNull(readerCreator, "readerCreator");
    }

    /**
     * Serializes a packet into a byte array.
     *
     * @param packet The packet to serialize
     * @return The byte array representation of the packet
     */
    public byte[] serializePacket(@NotNull Packet packet) {
        DataWriter dataWriter = writerCreator.get();
        dataWriter.writeByte(packet.getId());
        packet.write(dataWriter);

        return dataWriter.toByteArray();
    }

    /**
     * Deserializes a packet from a byte array.
     *
     * @param bytes The byte array representation of the packet
     * @return An {@link Optional} of the deserialized packet which is empty if deserialization fails
     */
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
