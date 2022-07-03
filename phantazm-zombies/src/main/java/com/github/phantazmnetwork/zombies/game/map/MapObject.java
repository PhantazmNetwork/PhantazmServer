package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.steanky.ethylene.core.processor.ConfigProcessor;
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
     * The origin of this object, which is the point any of this object's coordinates are considered relative to
     */
    protected final Vec3I origin;

    /**
     * The instance which this MapObject is in.
     */
    protected final Instance instance;

    /**
     * Constructs a new instance of this class.
     * @param data the backing data object
     * @param origin the origin vector this object's coordinates are considered relative to
     * @param instance the instance which this MapObject is in
     */
    public MapObject(@NotNull TData data, @NotNull Vec3I origin, @NotNull Instance instance) {
        this.data = Objects.requireNonNull(data, "data");
        this.origin = Objects.requireNonNull(origin, "origin");
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    /**
     * Gets the data object.
     * @return the data object
     */
    public @NotNull TData getData() {
        return data;
    }

    /**
     * Gets the vector this object's coordinates are considered relative to.
     * @return the origin vector
     */
    public @NotNull Vec3I getOrigin() {
        return origin;
    }

    /**
     * Gets the instance this map object is in.
     * @return the instance this map object is in
     */
    public @NotNull Instance getInstance() {
        return instance;
    }
}
