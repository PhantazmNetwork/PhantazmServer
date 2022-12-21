package org.phantazm.zombies.map.shop;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PlayerInteraction extends Keyed {
    @NotNull ZombiesPlayer player();
}
