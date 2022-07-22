package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import com.github.phantazmnetwork.zombies.map.ShopInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class InteractorBase<TData extends Prioritized> implements ShopInteractor {
    protected final TData data;
    protected final ShopInfo shopInfo;
    protected final ZombiesMap.Context context;


    public InteractorBase(@NotNull TData data, @NotNull ShopInfo shopInfo, @NotNull ZombiesMap.Context context) {
        this.data = Objects.requireNonNull(data, "data");
        this.shopInfo = Objects.requireNonNull(shopInfo, "shopInfo");
        this.context = Objects.requireNonNull(context, "context");
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public int priority() {
        return data.priority();
    }
}
