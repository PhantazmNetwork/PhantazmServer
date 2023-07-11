package org.phantazm.messaging.packet.c2s;

import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.serialization.DataReader;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

public record RoundStartPacket() implements Packet {

    public static final byte ID = 0;

    public static @NotNull RoundStartPacket read(@NotNull DataReader reader) {
        Objects.requireNonNull(reader, "reader");
        return new RoundStartPacket();
    }

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public void write(@NotNull DataWriter dataWriter) {

    }
}
