package org.phantazm.zombies.map.shop.display;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

public interface ShopDisplay extends Tickable {
    void initialize(@NotNull Shop shop);

    void destroy(@NotNull Shop shop);

    default void update(@NotNull Shop shop, @NotNull PlayerInteraction interaction, boolean interacted) {
    }

    @Override
    default void tick(long time) {
    }
}
