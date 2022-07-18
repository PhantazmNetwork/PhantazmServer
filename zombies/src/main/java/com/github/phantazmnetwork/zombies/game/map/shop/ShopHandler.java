package com.github.phantazmnetwork.zombies.game.map.shop;

import com.github.phantazmnetwork.commons.Tickable;
import org.jetbrains.annotations.NotNull;

public interface ShopHandler extends Tickable {
    void initialize();

    void handleInteraction(@NotNull PlayerInteraction interaction);
}
