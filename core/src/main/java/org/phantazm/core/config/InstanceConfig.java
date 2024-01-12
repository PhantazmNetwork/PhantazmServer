package org.phantazm.core.config;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

/**
 * Config for a single {@link Instance}.
 */
public record InstanceConfig(long time,
    @NotNull Pos spawnPoint,
    int timeRate,
    int chunkLoadDistance) {

    @Default("timeRate")
    public static @NotNull ConfigElement defaultTimeRate() {
        return ConfigPrimitive.of(0);
    }
}
