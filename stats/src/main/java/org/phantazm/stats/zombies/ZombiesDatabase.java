package org.phantazm.stats.zombies;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ZombiesDatabase {

    @NotNull CompletableFuture<Void> synchronizeZombiesPlayerMapStats(@NotNull ZombiesPlayerMapStats stats,
                                                                      int playerCount, @Nullable String category, @Nullable Long time);

    @NotNull CompletableFuture<ZombiesPlayerMapStats> getMapStats(@NotNull UUID playerUUID, @NotNull Key mapKey);

    @NotNull CompletableFuture<Int2ObjectMap<List<BestTime>>> getMapBestTimes(@NotNull Key mapKey, int minPlayerCount, int maxPlayerCount, @Nullable String category, int maxLength);

    @NotNull CompletableFuture<Int2ObjectMap<BestTime>> getMapPlayerBestTimes(@NotNull UUID playerUUID, @NotNull Key mapKey, int minPlayerCount, int maxPlayerCount, @Nullable String category);


}
