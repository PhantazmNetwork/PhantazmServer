package com.github.phantazmnetwork.velocity.packet;

import com.github.phantazmnetwork.messaging.packet.DataWriter;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ByteArrayOutputDataWriter implements DataWriter {

    private final ByteArrayDataOutput output;

    public ByteArrayOutputDataWriter(@NotNull ByteArrayDataOutput output) {
        this.output = Objects.requireNonNull(output, "output");
    }

    @Override
    public void writeByte(byte data) {
        output.writeByte(data);
    }

    @Override
    public void writeInt(int data) {
        output.writeInt(data);
    }

    @Override
    public byte @NotNull [] toByteArray() {
        return output.toByteArray();
    }
}
