package org.phantazm.messaging.packet;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.serialization.DataWriter;

/**
 * Represents a packet that can be sent through plugin messaging.
 */
public interface Packet {

    @NotNull Key getId();

    /**
     * Writes the packet to a {@link DataWriter}.
     *
     * @param dataWriter The {@link DataWriter} to write to
     */
    void write(@NotNull DataWriter dataWriter);
}
