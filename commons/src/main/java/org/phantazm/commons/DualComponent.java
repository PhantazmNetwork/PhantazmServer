package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public interface DualComponent<T, R> extends BiFunction<@NotNull InjectionStore, @NotNull T, @NotNull R> {
}
