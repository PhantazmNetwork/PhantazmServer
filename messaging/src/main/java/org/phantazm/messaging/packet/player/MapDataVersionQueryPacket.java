package org.phantazm.messaging.packet.player;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.messaging.packet.Packet;
import org.phantazm.messaging.serialization.DataReader;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

/**
 * A packet that queries for the server's current version of map data.
 */
public record MapDataVersionQueryPacket() implements Packet {

    public static final Key ID = Key.key(Namespaces.PHANTAZM, "client/mapdata_version_query");

    public static @NotNull MapDataVersionQueryPacket read(@NotNull DataReader reader) {
        Objects.requireNonNull(reader);
        return new MapDataVersionQueryPacket();
    }

    @Override
    public @NotNull Key getId() {
        return ID;
    }

    @Override
    public void write(@NotNull DataWriter dataWriter) {

    }
}
