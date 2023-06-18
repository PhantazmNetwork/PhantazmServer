package org.phantazm.zombies.stats;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ZombiesDatabase {

    @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats);

    CompletableFuture<List<BestTime>> getBestTimes(@NotNull Key mapKey);
}
