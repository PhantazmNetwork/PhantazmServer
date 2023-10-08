package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface MonoComponent<T> extends Function<@NotNull InjectionStore, @NotNull T> {
}
