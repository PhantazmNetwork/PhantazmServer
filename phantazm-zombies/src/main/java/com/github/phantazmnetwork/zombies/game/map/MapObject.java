package com.github.phantazmnetwork.zombies.game.map;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a generic "map object" with some backing data.
 *
 * @param <TData> the type of data backing the object
 */
public class MapObject<TData> {
    /**
     * The data object itself.
     */
    protected final TData data;

    /**
     * Constructs a new instance of this class.
     *
     * @param data the backing data object
     */
    public MapObject(@NotNull TData data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    /**
     * Gets the data object.
     *
     * @return the data object
     */
    public @NotNull TData getData() {
        return data;
    }
}
