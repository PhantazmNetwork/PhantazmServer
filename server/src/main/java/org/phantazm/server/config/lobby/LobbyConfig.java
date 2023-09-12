package org.phantazm.server.config.lobby;

import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.config.InstanceConfig;

import java.util.List;

public record LobbyConfig(
    @NotNull Key name,
    @NotNull List<String> lobbyPaths,
    int maxPlayers,
    int maxLobbies,
    int timeout,
    @NotNull List<ItemStack> defaultItems,
    @NotNull String lobbyJoinFormat,
    @NotNull InstanceConfig instanceConfig) {

    @Default("timeout")
    public static @NotNull ConfigPrimitive timeoutDefault() {
        return ConfigPrimitive.of(20);
    }
}
