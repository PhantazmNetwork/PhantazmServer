package org.phantazm.stats.zombies;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ZombiesDatabase {

    @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats,
            int playerCount, @Nullable String category, @Nullable Long time);

    @NotNull CompletableFuture<ZombiesPlayerMapStats> getMapStats(@NotNull UUID playerUUID, @NotNull Key mapKey);

    @NotNull CompletableFuture<List<BestTime>> getBestTimes(@NotNull Key mapKey, int playerCount, @Nullable String category, int maxLength);

    @NotNull CompletableFuture<Optional<BestTime>> getBestTime(@NotNull UUID playerUUID, @NotNull Key mapKey, int playerCount, @Nullable String category);


}
