package org.phantazm.messaging.serialization;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a data output that can be converted to a byte array. Implementations should typically be wrappers around
 * provided binary writing libraries that already exist on respective platforms.
 */
public interface DataWriter {

    /**
     * Writes a byte.
     *
     * @param data The byte to write
     */
    void writeByte(byte data);

    /**
     * Writes an int.
     *
     * @param data The int to write
     */
    void writeInt(int data);

    /**
     * Converts the writer to a byte array.
     *
     * @return The byte array representation of the writer
     */
    byte @NotNull [] toByteArray();

}
