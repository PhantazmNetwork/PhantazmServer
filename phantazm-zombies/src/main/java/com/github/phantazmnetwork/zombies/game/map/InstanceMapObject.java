package com.github.phantazmnetwork.zombies.game.map;

import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class InstanceMapObject<TData> extends MapObject<TData> {
    /**
     * The instance which this MapObject is in.
     */
    protected final Instance instance;


    /**
     * Constructs a new instance of this class.
     *
     * @param data the backing data object
     */
    public InstanceMapObject(@NotNull TData data, @NotNull Instance instance) {
        super(data);
        this.instance = Objects.requireNonNull(instance, "instance");
    }

    /**
     * Gets the instance this map object is in.
     *
     * @return the instance this map object is in
     */
    public @NotNull Instance getInstance() {
        return instance;
    }
}
