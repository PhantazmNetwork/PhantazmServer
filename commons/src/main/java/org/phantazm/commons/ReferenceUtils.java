package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public final class ReferenceUtils {
    private static final Reference<?> NULL_REFERENCE = new WeakReference<>(null);

    private ReferenceUtils() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull Reference<T> nullReference() {
        return (Reference<T>) NULL_REFERENCE;
    }
}
