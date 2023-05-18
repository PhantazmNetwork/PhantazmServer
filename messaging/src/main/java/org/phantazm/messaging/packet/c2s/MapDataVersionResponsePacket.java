package org.phantazm.messaging.packet.c2s;

import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.serialization.DataReader;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

/**
 * A packet that indicates the current version of map data.
 *
 * @param version The current version of map data
 */
public record MapDataVersionResponsePacket(int version) implements Packet {

    /**
     * The ID of the {@link MapDataVersionResponsePacket}.
     */
    public static final byte ID = 1;

    public static @NotNull MapDataVersionResponsePacket read(@NotNull DataReader reader) {
        Objects.requireNonNull(reader, "reader");
        return new MapDataVersionResponsePacket(reader.readInt());
    }

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public void write(@NotNull DataWriter dataWriter) {
        dataWriter.writeInt(version);
    }
}
