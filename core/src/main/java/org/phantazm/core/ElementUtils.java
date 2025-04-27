package org.phantazm.core;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.function.Consumer;

public final class ElementUtils {
    private ElementUtils() {
    }

    public static @NotNull Consumer<? super ElementException> logging(@NotNull Logger logger, @NotNull String name) {
        return (e) -> logger.warn("Error when loading " + name + " element(s)", e);
    }

    public static @NotNull Optional<String> findModel(@NotNull Class<?> cls) {
        for (Annotation annotation : cls.getDeclaredAnnotations()) {
            if (annotation instanceof Model model) return Optional.of(model.value());
        }

        return Optional.empty();
    }
}
