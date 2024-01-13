package org.phantazm.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public interface ObjectExtractor<T, V extends ConfigElement> {
    @FunctionalInterface
    interface Handler<T, V extends ConfigElement> {
        @NotNull Collection<@NotNull Entry<T>> handle(@NotNull DataLocation location, @NotNull V element) throws IOException;
    }

    @NotNull Class<V> allowedType();

    @NotNull Collection<@NotNull Entry<T>> extract(@NotNull DataLocation location, @NotNull V element) throws IOException;

    static <T, V extends ConfigElement> @NotNull ObjectExtractor<T, V> extractor(@NotNull Class<V> type,
        @NotNull Handler<T, V> handler) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(handler);
        return new ObjectExtractor<>() {
            @Override
            public @NotNull Class<V> allowedType() {
                return type;
            }

            @Override
            public @NotNull Collection<@NotNull Entry<T>> extract(@NotNull DataLocation location, @NotNull V element) throws IOException {
                return Objects.requireNonNull(handler.handle(location, element));
            }
        };
    }

    static <T> @NotNull Entry<T> entry(@NotNull Key identifier, @NotNull T object) {
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(object);
        return new Entry<>(identifier, object);
    }

    static <T> @NotNull Entry<T> anonymousEntry(@NotNull T object) {
        Objects.requireNonNull(object);
        return new Entry<>(null, object);
    }

    record Entry<T>(@Nullable Key identifier,
        @NotNull T object) {
    }
}
