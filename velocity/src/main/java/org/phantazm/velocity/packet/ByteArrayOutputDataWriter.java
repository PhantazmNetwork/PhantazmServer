package org.phantazm.velocity.packet;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

/**
 * A {@link DataWriter} that wraps around a {@link ByteArrayDataOutput}.
 */
public class ByteArrayOutputDataWriter implements DataWriter {

    private final ByteArrayDataOutput output;

    /**
     * Creates a {@link ByteArrayOutputDataWriter}.
     *
     * @param output The delegate {@link ByteArrayDataOutput}
     */
    public ByteArrayOutputDataWriter(@NotNull ByteArrayDataOutput output) {
        this.output = Objects.requireNonNull(output, "output");
    }

    public ByteArrayOutputDataWriter() {
        this(ByteStreams.newDataOutput());
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
