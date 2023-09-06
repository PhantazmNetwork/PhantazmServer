package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface BasicComponent<T> extends Function<@NotNull InjectionStore, T> {
}
