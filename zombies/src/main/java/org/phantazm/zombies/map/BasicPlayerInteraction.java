package org.phantazm.zombies.map;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.player.ZombiesPlayer;

public record BasicPlayerInteraction(@NotNull ZombiesPlayer player, @NotNull Key key) implements PlayerInteraction {

}
