package org.phantazm.core.config;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Config for a single {@link Instance}.
 */
public record InstanceConfig(@NotNull Pos spawnPoint, long time, int timeRate, int chunkLoadDistance) {
    /**
     * The default spawn point {@link Pos}.
     */
    public static final Pos DEFAULT_POS = Pos.ZERO;

    public static final long DEFAULT_TIME = 0;

    public static final int DEFAULT_TIME_RATE = 0;

    public static final int DEFAULT_CHUNK_LOAD_RANGE = 10;

    /**
     * Creates config regarding a single {@link Instance}.
     *
     * @param spawnPoint The spawn point for the {@link Instance}
     */
    public InstanceConfig {
        Objects.requireNonNull(spawnPoint);
    }

}
