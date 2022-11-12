package com.github.phantazmnetwork.zombies.map.shop.interactor;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.map.shop.PlayerInteraction;
import org.jetbrains.annotations.NotNull;

public interface ShopInteractor extends Tickable {
    void handleInteraction(@NotNull PlayerInteraction interaction);

    @Override
    default void tick(long time) {

    }
}
