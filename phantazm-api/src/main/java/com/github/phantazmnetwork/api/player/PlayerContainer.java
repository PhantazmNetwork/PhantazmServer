package com.github.phantazmnetwork.api.player;

import net.minestom.server.entity.Player;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Container to store {@link Player}s based on their {@link UUID}.
 */
public interface PlayerContainer {

    /**
     * Gets the {@link Player} with the given {@link UUID}.
     * @param uuid The {@link UUID} of the {@link Player}.
     * @return The {@link Player}
     */
    @Nullable Player getPlayer(@NotNull UUID uuid);

    @Nullable Player getPlayer(@NotNull String name);
}
