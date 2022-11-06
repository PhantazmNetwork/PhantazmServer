package com.github.phantazmnetwork.messaging.packet;

import com.github.phantazmnetwork.messaging.serialization.DataWriter;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a packet that can be sent through plugin messaging.
 */
public interface Packet {

    /**
     * Gets the ID of the packet. This should be unique for a single messaging channel.
     * For example, all the packets in the proxy plugin messaging channel should be unique.
     * However, they can share IDs with packets on other channels.
     *
     * @return The ID of the packet
     */
    byte getId();

    /**
     * Writes the packet to a {@link DataWriter}.
     *
     * @param dataWriter The {@link DataWriter} to write to
     */
    void write(@NotNull DataWriter dataWriter);
}
