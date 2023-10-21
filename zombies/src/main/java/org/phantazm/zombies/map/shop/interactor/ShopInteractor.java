package org.phantazm.zombies.map.shop.interactor;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.shop.PlayerInteraction;
import org.phantazm.zombies.map.shop.Shop;

public interface ShopInteractor extends Tickable {
    static boolean handle(@NotNull Iterable<? extends ShopInteractor> interactors,
        @NotNull PlayerInteraction interaction) {
        boolean res = true;
        for (ShopInteractor interactor : interactors) {
            res &= interactor.handleInteraction(interaction);
        }

        return res;
    }

    static void tick(@NotNull Iterable<? extends ShopInteractor> interactors, long time) {
        for (ShopInteractor interactor : interactors) {
            interactor.tick(time);
        }
    }

    static void initialize(@NotNull Iterable<? extends ShopInteractor> interactors, @NotNull Shop shop) {
        for (ShopInteractor interactor : interactors) {
            interactor.initialize(shop);
        }
    }

    boolean handleInteraction(@NotNull PlayerInteraction interaction);

    default void initialize(@NotNull Shop shop) {
    }

    default void tick(long time) {
        
    }
}
