package com.github.phantazmnetwork.mob.target;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

/**
 * A {@link MappedSelector} that selects the first target from an {@link Iterable} of targets.
 * @param <TTarget> The type of the target to select
 */
public class FirstTargetSelector<TTarget> extends MappedSelector<Iterable<TTarget>, TTarget> {

    /**
     * Creates a new {@link FirstTargetSelector}.
     * @param delegate The delegate {@link TargetSelector} to map
     */
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
