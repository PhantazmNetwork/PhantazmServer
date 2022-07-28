package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import org.jetbrains.annotations.NotNull;

public interface ShopInteractor extends Tickable, Prioritized {
    void handleInteraction(@NotNull PlayerInteraction interaction);
}
