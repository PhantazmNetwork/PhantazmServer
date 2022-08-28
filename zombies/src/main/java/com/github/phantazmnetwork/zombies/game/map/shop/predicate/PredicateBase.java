package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class PredicateBase<TData> implements ShopPredicate {
    protected final TData data;

    public PredicateBase(@NotNull TData data) {
        this.data = Objects.requireNonNull(data, "data");
    }
}
