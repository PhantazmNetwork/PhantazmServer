package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record BasicPlayerInteraction(@NotNull ZombiesPlayer player, @NotNull Key key) implements PlayerInteraction {

}
