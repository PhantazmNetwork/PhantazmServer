package org.phantazm.messaging.serialization;

/**
 * Represents a data input.
 * Implementations should typically be wrappers around provided binary reading libraries
 * that already exist on respective platforms.
 */
public interface DataReader {

    /**
     * Reads a byte from the input.
     *
     * @return The byte
     */
    byte readByte();

    /**
     * Reads an int from the input.
     *
     * @return The int
     */
    int readInt();

}
