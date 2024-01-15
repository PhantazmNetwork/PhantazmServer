package org.phantazm.server.context;

import org.jetbrains.annotations.NotNull;
import org.phantazm.stats.general.GeneralDatabase;
import org.phantazm.stats.zombies.ZombiesLeaderboardDatabase;
import org.phantazm.stats.zombies.ZombiesStatsDatabase;

public record DatabaseAccessContext(@NotNull GeneralDatabase generalDatabase,
    @NotNull ZombiesLeaderboardDatabase zombiesLeaderboardDatabase,
    @NotNull ZombiesStatsDatabase zombiesStatsDatabase) {
}
