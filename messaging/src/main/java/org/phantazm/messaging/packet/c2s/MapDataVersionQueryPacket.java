package org.phantazm.messaging.packet.c2s;

import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.serialization.DataReader;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

/**
 * A packet that queries for the server's current version of map data.
 */
public record MapDataVersionQueryPacket() implements Packet {

    /**
     * The ID of the {@link MapDataVersionQueryPacket}.
     */
    public static final byte ID = 0;

    public static @NotNull MapDataVersionQueryPacket read(@NotNull DataReader reader) {
        Objects.requireNonNull(reader, "reader");
        return new MapDataVersionQueryPacket();
    }

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public void write(@NotNull DataWriter dataWriter) {

    }
}
