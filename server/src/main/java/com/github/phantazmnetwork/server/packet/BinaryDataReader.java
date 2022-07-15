package com.github.phantazmnetwork.server.packet;

import com.github.phantazmnetwork.messaging.packet.DataReader;
import net.minestom.server.utils.binary.BinaryReader;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BinaryDataReader implements DataReader {

    private final BinaryReader binaryReader;

    public BinaryDataReader(@NotNull BinaryReader binaryReader) {
        this.binaryReader = Objects.requireNonNull(binaryReader, "binaryReader");
    }

    @Override
    public byte readByte() {
        return binaryReader.readByte();
    }

    @Override
    public int readInt() {
        return binaryReader.readInt();
    }
}
