package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public interface PlayerInteraction {
    @NotNull Key getType();

    @NotNull ZombiesPlayer getPlayer();
}
