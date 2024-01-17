package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GeneralDatabase {
    @NotNull CompletableFuture<Void> initTables();

    @NotNull CompletableFuture<Void> handleJoin(@NotNull UUID playerUUID);

    @NotNull CompletableFuture<Void> updateJoin(@NotNull UUID playerUUID, long firstJoin, long lastJoin);
}
