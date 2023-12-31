package org.phantazm.stats.zombies;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Performs database queries pertaining to leaderboard times.
 */
public interface LeaderboardDatabase {
    record LeaderboardEntry(@NotNull Set<@NotNull UUID> team,
        long timeTaken,
        long timeEnd) {
    }

    @NotNull CompletableFuture<Void> initTables(@NotNull IntSet teamSizes, @NotNull Set<String> validModifierKeys);

    @NotNull CompletableFuture<Optional<Long>> fetchBestTime(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey);

    @NotNull CompletableFuture<List<LeaderboardEntry>> fetchTimeHistory(@NotNull Set<UUID> team, @NotNull Key map,
        @NotNull String modifierKey);

    @NotNull CompletableFuture<List<LeaderboardEntry>> fetchEntries(int teamSize, @NotNull String modifierKey,
        @NotNull Key map, int start, int entries);

    @NotNull CompletableFuture<Void> submitGame(@NotNull Set<UUID> team, @NotNull String modifierKey, @NotNull Key map,
        long timeTaken, long timeEnd);
}
