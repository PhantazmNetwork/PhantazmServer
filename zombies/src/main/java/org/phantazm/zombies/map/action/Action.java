package org.phantazm.zombies.map.action;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Action<TData> {
    void perform(@NotNull TData data);
}
