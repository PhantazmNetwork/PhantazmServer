package org.phantazm.stats.general;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UsernameDatabase {
    @NotNull CompletableFuture<Void> initTables();

    @NotNull CompletableFuture<Optional<String>> cachedUsername(@NotNull UUID uuid);

    @NotNull CompletableFuture<Optional<UUID>> cachedUUID(@NotNull String username);

    @NotNull CompletableFuture<Void> submitUsername(@NotNull UUID uuid, @NotNull String username);
}