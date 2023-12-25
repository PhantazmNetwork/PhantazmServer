package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Performs database queries pertaining to leaderboard times.
 */
public interface LeaderboardDatabase {
    record LeaderboardEntry(@NotNull Set<@NotNull UUID> team,
        long timeTaken) {
    }

    @NotNull CompletableFuture<Long> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey);

    @NotNull CompletableFuture<List<LeaderboardEntry>> fetchEntries(int teamSize, @NotNull String modifierKey,
        @NotNull Key map, int start, int entries);

    @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey, @NotNull Key map,
        long timeTaken, long timeEnd);
}
