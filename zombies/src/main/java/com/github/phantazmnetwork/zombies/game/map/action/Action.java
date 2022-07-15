package com.github.phantazmnetwork.zombies.game.map.action;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an action performed on a particular object. Actions have a general concept of "priority", where if
 * multiple actions are to be executed, actions with a higher priority should be performed first.
 *
 * @param <TData> the type of data object this action acts on
 */
public interface Action<TData> extends Comparable<Action<?>> {
    void perform(@NotNull TData data);

    int priority();

    @Override
    default int compareTo(@NotNull Action<?> o) {
        return Integer.compare(priority(), o.priority());
    }
}
