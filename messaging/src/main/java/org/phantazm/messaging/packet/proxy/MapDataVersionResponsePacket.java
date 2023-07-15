package org.phantazm.messaging.packet.proxy;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
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

    public static final Key ID = Key.key(Namespaces.PHANTAZM, "proxy/mapdata_version_response");

    public static @NotNull MapDataVersionResponsePacket read(@NotNull DataReader reader) {
        Objects.requireNonNull(reader, "reader");
        return new MapDataVersionResponsePacket(reader.readInt());
    }

    @Override
    public @NotNull Key getId() {
        return ID;
    }

    @Override
    public void write(@NotNull DataWriter dataWriter) {
        dataWriter.writeInt(version);
    }
}
