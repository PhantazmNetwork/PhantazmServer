package com.github.phantazmnetwork.messaging.packet.c2s;

import com.github.phantazmnetwork.messaging.packet.Packet;
import com.github.phantazmnetwork.messaging.serialization.DataReader;
import com.github.phantazmnetwork.messaging.serialization.DataWriter;
import org.jetbrains.annotations.NotNull;

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
