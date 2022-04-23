package com.github.phantazmnetwork.api.player;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * A class that resolves names and UUIDs. May need to perform costly blocking I/O operations, so it is encouraged to
 * cache the results of calling the methods in this class, and to only call them in a non-blocking context.
 *
 * @implSpec Implementations should guarantee thread safety.
 */
@Blocking
public interface IdentitySource {
    /**
     * <p>Optionally resolves the given UUID to a name. If the UUID is invalid, or there is an IO related error, the
     * returned Optional will be empty.</p>
     * @param uuid the UUID of the player to resolve
     * @return an Optional instance containing the name of the player associated with the given UUID, which will be
     * empty if there is an IO problem or if the UUID is invalid
     */
    @NotNull Optional<String> getName(@NotNull UUID uuid);

    /**
     * Optionally resolves the given name to a UUID. If the name is invalid or does not exist, or there is an IO related
     * error, the returned Optional will be empty.
     * @param name the name of the player to resolve
     * @return an Optional instance containing the UUID of the player associated with the given name, which will be
     * empty if there is an IO problem or if the name is invalid
     */
    @NotNull Optional<UUID> getUUID(@NotNull String name);

    /**
     * A standard IdentitySource implementation that uses Mojang's API servers to resolve names and UUIDs.
     */
    IdentitySource MOJANG = new MojangIdentitySource();
}
