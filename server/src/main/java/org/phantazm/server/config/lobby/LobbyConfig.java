package org.phantazm.server.config.lobby;

import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.config.InstanceConfig;

import java.util.Collection;
import java.util.List;

public record LobbyConfig(
    @NotNull Key name,
    @NotNull List<String> lobbyPaths,
    int maxPlayers,
    int maxLobbies,
    @NotNull List<ItemStack> defaultItems,
    @NotNull String lobbyJoinFormat,
    @NotNull InstanceConfig instanceConfig) {

}
