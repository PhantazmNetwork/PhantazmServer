package org.phantazm.loader;

import com.github.steanky.ethylene.core.collection.ConfigContainer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

@FunctionalInterface
public interface ObjectExtractor<T> {
    @NotNull Collection<@NotNull Entry<T>> extract(@NotNull ConfigContainer container) throws IOException;

    record Entry<T>(@NotNull Key identifier,
        @NotNull T object) {
    }
}
