package org.phantazm.core.packet;

import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.utils.binary.BinaryWriter;
import org.jetbrains.annotations.NotNull;
import org.phantazm.messaging.serialization.DataReader;
import org.phantazm.messaging.serialization.DataWriter;

import java.util.Objects;

/**
 * A {@link DataReader} that wraps around a {@link BinaryWriter}.
 */
public class BinaryDataWriter implements DataWriter {

    private final BinaryWriter binaryWriter;

    /**
     * Creates a {@link BinaryDataWriter}.
     *
     * @param binaryWriter The delegate {@link BinaryWriter}
     */
    public BinaryDataWriter(@NotNull BinaryWriter binaryWriter) {
        this.binaryWriter = Objects.requireNonNull(binaryWriter);
    }

    public static @NotNull BinaryDataWriter fromNetworkBuffer(@NotNull NetworkBuffer networkBuffer) {
        return new BinaryDataWriter(new BinaryWriter(Objects.requireNonNull(networkBuffer)));
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
