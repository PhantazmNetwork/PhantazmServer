package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minestom.server.instance.Instance;
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
    protected final Vec3I mapOrigin;

    /**
     * The instance which this MapObject is in.
     */
    protected final Instance instance;

    /**
     * Constructs a new instance of this class.
     * @param data the backing data object
     * @param mapOrigin the origin vector for the map this object is part of
     * @param instance the instance which this MapObject is in
     */
    public MapObject(@NotNull TData data, @NotNull Vec3I mapOrigin, @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.mapOrigin = Objects.requireNonNull(mapOrigin, "origin");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    /**
     * Gets the data object.
     * @return the data object
     */
    public @NotNull TData getData() {
        return data;
    }
}
