package org.phantazm.zombies.map.shop.predicate;

import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

import java.util.function.BiPredicate;

public interface ShopPredicate extends BiPredicate<PlayerInteraction, Shop> {
    boolean canInteract(@NotNull PlayerInteraction interaction, @NotNull Shop shop);

    @Override
    default boolean test(PlayerInteraction interaction, Shop shop) {
        return canInteract(interaction, shop);
    }
}
