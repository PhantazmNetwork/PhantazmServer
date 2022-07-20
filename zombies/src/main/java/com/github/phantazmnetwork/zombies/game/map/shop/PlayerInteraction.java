package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface PlayerInteraction extends Keyed {
    @NotNull ZombiesPlayer getPlayer();
}
