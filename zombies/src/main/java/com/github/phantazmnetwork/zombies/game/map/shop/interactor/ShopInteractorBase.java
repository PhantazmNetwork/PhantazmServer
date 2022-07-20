package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class ShopInteractorBase implements ShopInteractor {
    protected final ShopInfo shopInfo;
    protected final ZombiesMap.Context context;

    public ShopInteractorBase(@NotNull ShopInfo shopInfo, @NotNull ZombiesMap.Context context) {
        this.shopInfo = Objects.requireNonNull(shopInfo, "shopInfo");
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public void tick(long time) {
    }
}
