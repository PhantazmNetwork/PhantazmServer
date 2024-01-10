package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GeneralDatabase {
    void initTables();

    @NotNull CompletableFuture<Void> handleJoin(@NotNull UUID playerUUID, @NotNull ZonedDateTime time);

}
