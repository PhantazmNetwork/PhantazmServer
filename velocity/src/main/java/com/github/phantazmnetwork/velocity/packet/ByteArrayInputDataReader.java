package com.github.phantazmnetwork.velocity.packet;

import com.github.phantazmnetwork.messaging.packet.DataReader;
import com.google.common.io.ByteArrayDataInput;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link DataReader} that wraps around a {@link ByteArrayDataInput}.
 */
public class ByteArrayInputDataReader implements DataReader {

    private final ByteArrayDataInput input;

    /**
     * Creates a {@link ByteArrayDataInput}.
     *
     * @param input The delegate {@link ByteArrayDataInput}
     */
    public ByteArrayInputDataReader(@NotNull ByteArrayDataInput input) {
        this.input = Objects.requireNonNull(input, "input");
    }

    @Override
    public byte readByte() {
        return input.readByte();
    }

    @Override
    public int readInt() {
        return input.readInt();
    }
}
