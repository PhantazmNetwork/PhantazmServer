package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public interface ShopPredicate extends Predicate<PlayerInteraction> {
    boolean canInteract(@NotNull PlayerInteraction interaction);

    @Override
    default boolean test(PlayerInteraction interaction) {
        return canInteract(interaction);
    }
}
