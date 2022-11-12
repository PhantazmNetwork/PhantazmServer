package com.github.phantazmnetwork.zombies.map.shop;

import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface PlayerInteraction extends Keyed {
    @NotNull ZombiesPlayer player();
}
