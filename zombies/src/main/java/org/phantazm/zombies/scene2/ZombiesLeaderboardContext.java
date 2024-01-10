package org.phantazm.zombies.scene2;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MonoComponent;
import org.phantazm.core.leaderboard.Leaderboard;
import org.phantazm.stats.zombies.ZombiesLeaderboardDatabase;

import java.util.concurrent.Executor;

public record ZombiesLeaderboardContext(@NotNull Executor executor,
    @NotNull ZombiesLeaderboardDatabase database,
    @NotNull MonoComponent<Leaderboard> bestTimeLeaderboard) {
}
