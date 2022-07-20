package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import org.jetbrains.annotations.NotNull;

public interface ShopInteractor extends Tickable {
    boolean handleInteraction(@NotNull Shop shop, @NotNull PlayerInteraction interaction);
}
