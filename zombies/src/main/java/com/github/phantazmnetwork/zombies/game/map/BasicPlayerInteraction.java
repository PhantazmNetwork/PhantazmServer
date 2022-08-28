package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record BasicPlayerInteraction(@NotNull ZombiesPlayer player, @NotNull Key key) implements PlayerInteraction {

}
