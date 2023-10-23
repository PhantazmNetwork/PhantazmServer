package org.phantazm.server.config.zombies;

import org.jetbrains.annotations.NotNull;
import org.phantazm.server.config.server.ZombiesGamereportConfig;

public record ZombiesConfig(@NotNull ZombiesGamereportConfig gamereportConfig,
    int maximumScenes,
    long joinRatelimit) {

    public static final ZombiesConfig DEFAULT = new ZombiesConfig(ZombiesGamereportConfig.DEFAULT, 20, 1000);

}
