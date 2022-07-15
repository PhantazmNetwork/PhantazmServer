package com.github.phantazmnetwork.velocity.packet;

import com.github.phantazmnetwork.messaging.packet.DataReader;
import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ByteBufDataReader implements DataReader {

    private final ByteBuf buf;

    public ByteBufDataReader(@NotNull ByteBuf buf) {
        this.buf = Objects.requireNonNull(buf, "buf");
    }

    @Override
    public byte readByte() {
        return buf.readByte();
    }

    @Override
    public int readInt() {
        return buf.readInt();
    }
}
