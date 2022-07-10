package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link MapObject} that has a position and is tied to a particular instance.
 * @param <TData> the type of data
 */
public abstract class PositionalMapObject<TData> extends MapObject<TData> {
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
     *
     * @param data     the backing data object
     * @param origin   the origin vector this object's coordinates are considered relative to
     * @param instance the instance which this MapObject is in
     */
    public PositionalMapObject(@NotNull TData data, @NotNull Vec3I origin, @NotNull Instance instance) {
        super(data);
        this.origin = Objects.requireNonNull(origin, "origin");
        this.instance = Objects.requireNonNull(instance, "instance");
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
