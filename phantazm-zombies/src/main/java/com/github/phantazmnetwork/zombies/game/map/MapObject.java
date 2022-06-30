package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a generic "map object" such as a window, door, shop, etc.
 * @param <TData> the type of data backing the object
 */
public class MapObject<TData> {
    /**
     * The data object itself.
     */
    protected final TData data;

    /**
     * The origin of the map to which this object belongs.
     */
    protected final Vec3I origin;

    /**
     * Constructs a new instance of this class.
     * @param data the backing data object
     */
    public MapObject(@NotNull TData data, @NotNull Vec3I origin) {
        this.data = Objects.requireNonNull(data, "data");
        this.origin = Objects.requireNonNull(origin, "origin");
    }

    /**
     * Gets the data object.
     * @return the data object
     */
    public @NotNull TData getData() {
        return data;
    }
}
