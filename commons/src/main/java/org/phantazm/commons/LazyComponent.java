package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public interface LazyComponent<T, R> extends DualComponent<Supplier<@NotNull T>, R> {
}
