package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.InteractionType;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.map.ShopPositionInfo;
import org.jetbrains.annotations.NotNull;

public interface Shop extends Tickable {
    @NotNull ShopPositionInfo shopInfo();

    boolean activate(@NotNull ZombiesPlayer player, @NotNull InteractionType interaction);
}
