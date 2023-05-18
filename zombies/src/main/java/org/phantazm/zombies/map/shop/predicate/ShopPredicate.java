package org.phantazm.zombies.map.shop.predicate;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;

import java.util.function.Predicate;

public interface ShopPredicate extends Predicate<PlayerInteraction> {
    boolean canInteract(@NotNull PlayerInteraction interaction);

    @Override
    default boolean test(PlayerInteraction interaction) {
        return canInteract(interaction);
    }
}
