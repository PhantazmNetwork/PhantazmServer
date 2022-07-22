package com.github.phantazmnetwork.zombies.game.map.shop.predicate;

import com.github.phantazmnetwork.commons.Prioritized;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class PredicateBase<TData extends Prioritized> implements ShopPredicate {
    protected final TData data;

    public PredicateBase(@NotNull TData data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public int priority() {
        return data.priority();
    }
}
