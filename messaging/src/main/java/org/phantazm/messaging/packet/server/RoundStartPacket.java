package org.phantazm.messaging.packet.server;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.serialization.DataReader;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

public record RoundStartPacket() implements Packet {

    public static final Key ID = Key.key(Namespaces.PHANTAZM, "server/round_start");

    public static @NotNull RoundStartPacket read(@NotNull DataReader reader) {
        Objects.requireNonNull(reader, "reader");
        return new RoundStartPacket();
    }

    @Override
    public @NotNull Key getId() {
        return ID;
    }

    @Override
    public void write(@NotNull DataWriter dataWriter) {

    }
}
