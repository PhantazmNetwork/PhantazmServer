package org.phantazm.zombies.map.action;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an action performed on a particular object.
 *
 * @param <TData> the type of data object this action acts on
 */
@FunctionalInterface
public interface Action<TData> {
    void perform(@NotNull TData data);
}
