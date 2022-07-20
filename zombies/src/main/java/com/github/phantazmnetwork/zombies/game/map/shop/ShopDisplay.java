package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Tickable;
import org.jetbrains.annotations.NotNull;

public interface ShopDisplay extends Tickable {
    void initialize(@NotNull Shop shop);

    void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction);
}
