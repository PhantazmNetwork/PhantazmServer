package org.phantazm.core.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Config for a single {@link Instance}.
 */
public record InstanceConfig(long time,
    @NotNull Pos spawnPoint,
    int timeRate,
    int chunkLoadDistance) {
    /**
     * The default spawn point {@link Pos}.
     */
    public static final Pos DEFAULT_POS = Pos.ZERO;

    public static final long DEFAULT_TIME = 0;

    public static final int DEFAULT_TIME_RATE = 0;

    public static final int DEFAULT_CHUNK_LOAD_RANGE = 10;

    @Default("timeRate")
    public static @NotNull ConfigElement defaultTimeRate() {
        return ConfigPrimitive.of(0);
    }
}
