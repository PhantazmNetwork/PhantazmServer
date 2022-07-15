package com.github.phantazmnetwork.messaging.proxy;

import com.github.phantazmnetwork.messaging.packet.DataReader;
import com.github.phantazmnetwork.messaging.packet.DataWriter;
import com.github.phantazmnetwork.messaging.packet.Packet;
import org.jetbrains.annotations.NotNull;

public record ForwardProtocolVersionPacket(int protocolVersion) implements Packet {

    public ForwardProtocolVersionPacket(@NotNull DataReader dataReader) {
        this(dataReader.readInt());
    }

    public static final byte ID = 0x00;

    @Override
    public byte getId() {
        return ID;
    }

    @Override
    public void write(@NotNull DataWriter dataWriter) {
        dataWriter.writeInt(protocolVersion);
    }
}
