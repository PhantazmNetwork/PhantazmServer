package com.github.phantazmnetwork.messaging.packet;

import org.jetbrains.annotations.NotNull;

public interface DataWriter {

    void writeByte(byte data);

    void writeInt(int data);

    byte @NotNull [] toByteArray();

}
