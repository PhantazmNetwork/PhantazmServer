package org.phantazm.messaging.packet;

import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.serialization.PacketSerializer;

import java.util.Objects;

/**
 * A wrapper around a {@link PacketSerializer} that simplifies binary data reading and writing.
 *
 * @param <TOutputReceiver> The type of output that packets will be sent to
 */
public abstract class PacketHandler<TOutputReceiver> {

    private final PacketSerializer packetSerializer;

    public PacketHandler(@NotNull PacketSerializer packetSerializer) {
        this.packetSerializer = Objects.requireNonNull(packetSerializer, "packetSerializer");
    }

    /**
     * Handles incoming binary data.
     *
     * @param outputReceiver The output receiver for any packets that will be sent as a result of receiving this data
     * @param data           The data that was sent
     */
    public void handleData(@NotNull TOutputReceiver outputReceiver, byte @NotNull [] data) {
        packetSerializer.deserializePacket(data).ifPresent(packet -> handlePacket(outputReceiver, packet));
    }

    /**
     * Handles an incoming packet.
     *
     * @param outputReceiver The output receiver for any packets that will be sent as a result of receiving this packet
     * @param packet         The packet that was sent
     */
    protected abstract void handlePacket(@NotNull TOutputReceiver outputReceiver, @NotNull Packet packet);

    /**
     * Sends binary data to a receiver.
     *
     * @param outputReceiver The binary data receiver
     * @param data           The binary data to send
     */
    protected abstract void sendToReceiver(@NotNull TOutputReceiver outputReceiver, byte @NotNull [] data);

    /**
     * Sends an outgoing packet to the output receiver.
     *
     * @param outputReceiver The output receiver
     * @param packet         The packet to send
     */
    protected void output(@NotNull TOutputReceiver outputReceiver, @NotNull Packet packet) {
        sendToReceiver(outputReceiver, packetSerializer.serializePacket(packet));
    }

}
