package com.github.phantazmnetwork.zombies.game.map.shop.display;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import org.jetbrains.annotations.NotNull;

public interface ShopDisplay extends Tickable {
    void initialize(@NotNull Shop shop);

    void destroy(@NotNull Shop shop);

    default void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
    }

    @Override
    default void tick(long time) {
    }
}
