package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Container to store {@link Player}s based on their {@link UUID}.
 */
@FunctionalInterface
public interface PlayerContainer {

    /**
     * Gets the {@link Player} with the given {@link UUID}.
     * @param uuid The {@link UUID} of the {@link Player}.
     * @return The {@link Player}
     */
    Player getPlayer(@NotNull UUID uuid);

}
