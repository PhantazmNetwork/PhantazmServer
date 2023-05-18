package org.phantazm.core;

import com.github.steanky.element.core.ElementException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.function.Consumer;

public final class ElementUtils {
    private ElementUtils() {
    }

    public static @NotNull Consumer<? super ElementException> logging(@NotNull Logger logger, @NotNull String name) {
        return (e) -> logger.warn("Error when loading " + name + " element(s)", e);
    }
}
