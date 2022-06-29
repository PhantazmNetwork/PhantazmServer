package com.github.phantazmnetwork.mob.target;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class FirstTargetSelector<TTarget> extends MappedSelector<Iterable<TTarget>, TTarget> {

    public FirstTargetSelector(@NotNull TargetSelector<Iterable<TTarget>> delegate) {
        super(delegate);
    }

    @Override
    protected TTarget map(@NotNull Iterable<TTarget> players) {
        Iterator<TTarget> iterator = players.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null;
    }
}
