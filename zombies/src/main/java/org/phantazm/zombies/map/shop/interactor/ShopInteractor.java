package org.phantazm.zombies.map.shop.interactor;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Tickable;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

public interface ShopInteractor extends Tickable {
    void handleInteraction(@NotNull PlayerInteraction interaction);

    @Override
    default void tick(long time) {

    }

    default void initialize(@NotNull Shop shop) {
    }
}
