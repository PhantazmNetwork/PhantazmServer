package org.phantazm.server;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class FeatureUtils {
    private FeatureUtils() {
    }

    @Contract("null -> fail")
    static <T> @NotNull T check(@Nullable T object) {
        if (object != null) {
            return object;
        }

        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements.length < 3) {
            throw new IllegalStateException("Feature not yet initialized");
        }

        StackTraceElement caller = elements[2];

        try {
            throw new IllegalStateException(
                "Feature '" + Class.forName(caller.getClassName()).getSimpleName() + "' not yet initialized");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(caller.getClassName() + " not initialized yet");
        }
    }
}
