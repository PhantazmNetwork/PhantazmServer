package com.github.phantazmnetwork.zombies.game.map.action;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an action performed on a particular object. Actions have a general concept of "priority", where if
 * multiple actions are to be executed, actions with a higher priority should be performed first.
 * @param <TData>
 */
public interface Action<TData> extends Comparable<Action<?>> {
    void perform(@NotNull TData data);

    int priority();

    @Override
    default int compareTo(@NotNull Action<?> o) {
        return Integer.compare(priority(), o.priority());
    }
}