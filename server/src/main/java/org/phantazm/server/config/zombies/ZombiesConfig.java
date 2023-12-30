package org.phantazm.server.config.zombies;

import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.NotNull;
import org.phantazm.server.config.server.ZombiesGamereportConfig;

import java.util.Set;

public record ZombiesConfig(@NotNull ZombiesGamereportConfig gamereportConfig,
    int maximumScenes,
    long joinRatelimit,
    @NotNull IntSet teamSizes,
    @NotNull Set<String> trackedModifiers) {

    public static final ZombiesConfig DEFAULT = new ZombiesConfig(ZombiesGamereportConfig.DEFAULT, 20,
        1000, IntSet.of(), Set.of());

}
