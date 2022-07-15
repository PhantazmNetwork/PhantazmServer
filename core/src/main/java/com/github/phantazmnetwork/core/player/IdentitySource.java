package com.github.phantazmnetwork.core.player;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * A class that resolves names and UUIDs. May need to perform costly I/O operations, so it is encouraged to cache the
 * results of calling the methods in this class.
 *
 * @implSpec Implementations should guarantee thread safety.
 */
public interface IdentitySource {
    /**
     * A standard IdentitySource implementation that uses Mojang's API servers to resolve names and UUIDs.
     */
    IdentitySource MOJANG = new MojangIdentitySource(ForkJoinPool.commonPool());

    /**
     * Optionally resolves the given UUID to a username, typically asynchronously. If the UUID is invalid or does not
     * exist, or there is an IO related error, the result of the returned {@link CompletableFuture} will be empty.
     *
     * @param uuid the UUID to determine a username from
     * @return a CompletableFuture whose result is an Optional instance containing the username of the player associated
     * with the given UUID, which will be empty if there is an IO problem or if the UUID is invalid
     */
    @NotNull CompletableFuture<Optional<String>> getName(@NotNull UUID uuid);

    /**
     * Optionally resolves the given name to a UUID, typically asynchronously. If the name is invalid or does not exist,
     * or there is an IO related error, the result of the returned {@link CompletableFuture} will be empty.
     *
     * @param name the username to determine a UUID from
     * @return a CompletableFuture whose result is an Optional instance containing the UUID of the player associated
     * with the given name, which will be empty if there is an IO problem or if the name is invalid
     */
    @NotNull CompletableFuture<Optional<UUID>> getUUID(@NotNull String name);
}
