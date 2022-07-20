package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.map.shop.PlayerInteraction;
import com.github.phantazmnetwork.zombies.game.map.shop.Shop;
import org.jetbrains.annotations.NotNull;

public class UpgradeInteractor implements ShopInteractor {


    @Override
    public void tick(long time) {

    }

    @Override
    public boolean handleInteraction(@NotNull Shop shop, @NotNull PlayerInteraction interaction) {
        return false;
    }
}
