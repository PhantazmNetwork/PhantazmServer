package com.github.phantazmnetwork.api.config;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Config for a single {@link net.minestom.server.instance.Instance}.
 */
public record InstanceConfig(@NotNull Pos spawnPoint) {
    /**
     * The default spawn point {@link Pos}.
     */
    public static final Pos DEFAULT_POS = Pos.ZERO;

    /**
     * Creates config regarding a single {@link net.minestom.server.instance.Instance}.
     * @param spawnPoint The spawn point for the {@link net.minestom.server.instance.Instance}
     */
    public InstanceConfig {
        Objects.requireNonNull(spawnPoint, "spawnPoint");
    }

}
