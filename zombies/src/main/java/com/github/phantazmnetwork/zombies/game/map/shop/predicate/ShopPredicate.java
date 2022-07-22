package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import org.jetbrains.annotations.NotNull;

public interface ShopPredicate extends Prioritized {
    boolean canHandleInteraction(@NotNull PlayerInteraction interaction);
}
