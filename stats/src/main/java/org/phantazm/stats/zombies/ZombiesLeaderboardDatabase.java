package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Performs database queries pertaining to leaderboard times.
 */
public interface ZombiesLeaderboardDatabase {
    record LeaderboardEntry(@NotNull Set<@NotNull UUID> team,
        long timeTaken,
        long timeEnd) {
    }

    record RankingEntry(int rank,
        long timeTaken) {
    }

    @NotNull CompletableFuture<Void> initTables();

    @NotNull CompletableFuture<Optional<RankingEntry>> fetchBestRanking(@NotNull UUID player, int teamSize,
        @NotNull Key map, @NotNull String modifierKey);

    @NotNull CompletableFuture<OptionalLong> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey);

    @NotNull CompletableFuture<List<LeaderboardEntry>> fetchTimeHistory(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey);

    @NotNull CompletableFuture<List<LeaderboardEntry>> fetchBestTimes(int teamSize, @NotNull String modifierKey,
        @NotNull Key map, int start, int entries);

    @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey, @NotNull Key map,
        long timeTaken, long timeEnd);
}
