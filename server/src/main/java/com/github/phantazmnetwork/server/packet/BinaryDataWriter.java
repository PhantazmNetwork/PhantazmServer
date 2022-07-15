package com.github.phantazmnetwork.server.packet;

import com.github.phantazmnetwork.messaging.packet.DataWriter;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BinaryDataWriter implements DataWriter {

    private final BinaryWriter binaryWriter;

    public BinaryDataWriter(@NotNull BinaryWriter binaryWriter) {
        this.binaryWriter = Objects.requireNonNull(binaryWriter, "binaryWriter");
    }

    @Override
    public void writeByte(byte data) {
        binaryWriter.writeByte(data);
    }

    @Override
    public void writeInt(int data) {
        binaryWriter.writeInt(data);
    }

    @Override
    public byte @NotNull [] toByteArray() {
        return binaryWriter.toByteArray();
    }
}
