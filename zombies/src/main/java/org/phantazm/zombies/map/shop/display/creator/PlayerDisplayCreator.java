package org.phantazm.zombies.map.shop.display.creator;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.display.ShopDisplay;
import org.phantazm.zombies.player.ZombiesPlayer;

public interface PlayerDisplayCreator {
    @NotNull ShopDisplay forPlayer(@NotNull ZombiesPlayer zombiesPlayer);
}
