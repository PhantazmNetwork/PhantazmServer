package com.github.phantazmnetwork.zombies.game.map.shop.interactor;

import com.github.phantazmnetwork.commons.Prioritized;
import com.github.phantazmnetwork.zombies.game.map.ZombiesMap;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class InteractorBase<TData extends Prioritized> implements ShopInteractor {
    protected final TData data;
    protected final ZombiesMap map;

    public InteractorBase(@NotNull TData data, @NotNull ZombiesMap map) {
        this.data = Objects.requireNonNull(data, "data");
        this.map = Objects.requireNonNull(map, "map");
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public int priority() {
        return data.priority();
    }
}
