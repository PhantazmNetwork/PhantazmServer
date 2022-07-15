package com.github.phantazmnetwork.velocity.packet;

import com.github.phantazmnetwork.messaging.packet.DataWriter;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ByteBufDataWriter implements DataWriter {

    private final ByteBuf buf;

    public ByteBufDataWriter(@NotNull ByteBuf buf) {
        this.buf = Objects.requireNonNull(buf, "buf");
    }

    @Override
    public void writeByte(byte data) {
        buf.writeByte(data);
    }

    @Override
    public void writeInt(int data) {
        buf.writeInt(data);
    }

    @Override
    public byte @NotNull [] toByteArray() {
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        return bytes;
    }
}
