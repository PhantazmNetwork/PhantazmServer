package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Tickable;
import org.jetbrains.annotations.NotNull;

public interface ShopInteractor extends Tickable {
    boolean handleInteraction(@NotNull Shop shop, @NotNull PlayerInteraction interaction);
}
