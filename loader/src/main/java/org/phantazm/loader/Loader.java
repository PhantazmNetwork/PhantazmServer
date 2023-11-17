package org.phantazm.loader;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.toolkit.function.ExceptionHandler;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public interface Loader<T> {
    @NotNull @Unmodifiable Map<Key, T> data();

    void load() throws IOException;

    static <T, V extends ConfigElement> @NotNull Loader<T> loader(
        @NotNull Supplier<? extends @NotNull DataSource> dataSourceSupplier,
        @NotNull ObjectExtractor<T, V> extractor) {
        Objects.requireNonNull(dataSourceSupplier);
        Objects.requireNonNull(extractor);
        return new Impl<>(dataSourceSupplier, extractor);
    }

    class Impl<T, V extends ConfigElement> implements Loader<T> {
        private final Supplier<? extends DataSource> dataSourceSupplier;
        private final ObjectExtractor<T, V> extractor;

        private Map<Key, T> data;

        private Impl(Supplier<? extends @NotNull DataSource> dataSourceSupplier, ObjectExtractor<T, V> extractor) {
            this.dataSourceSupplier = dataSourceSupplier;
            this.extractor = extractor;
        }

        @Override
        public @NotNull @Unmodifiable Map<Key, T> data() {
            Map<Key, T> data = this.data;
            if (data == null) {
                throw new IllegalStateException("data has not been loaded yet");
            }

            return data;
        }

        @Override
        public void load() throws IOException {
            Map<Key, T> map = new HashMap<>();

            try (ExceptionHandler<IOException> handler = new ExceptionHandler<>(IOException.class)) {
                try (DataSource dataSource = dataSourceSupplier.get()) {
                    while (dataSource.hasNext()) {
                        ConfigElement element = dataSource.next();
                        DataLocation location = dataSource.lastLocation();

                        handler.run(() -> {
                            Collection<ObjectExtractor.Entry<T>> entries;

                            try {
                                entries = extract(location, extractor, element);
                            } catch (Exception exception) {
                                if (exception instanceof LoaderException loaderException) {
                                    throw loaderException;
                                }

                                throw LoaderException.builder()
                                    .withElement(element)
                                    .withMessage("exception when loading from container")
                                    .withDataLocation(location)
                                    .withCause(exception)
                                    .build();
                            }

                            for (ObjectExtractor.Entry<T> entry : entries) {
                                if (map.putIfAbsent(entry.identifier(), entry.object()) != null) {
                                    throw LoaderException.builder()
                                        .withElement(element)
                                        .withMessage("duplicate key " + entry.identifier())
                                        .withDataLocation(location)
                                        .build();
                                }
                            }
                        });
                    }
                }
            }

            this.data = Map.copyOf(map);
        }

        private static <T, V extends ConfigElement> Collection<ObjectExtractor.Entry<T>> extract(
            DataLocation dataLocation,
            ObjectExtractor<T, V> extractor, ConfigElement element) throws IOException {
            Class<V> type = extractor.allowedType();
            if (!type.isAssignableFrom(element.getClass())) {
                throw LoaderException.builder()
                    .withElement(element)
                    .withMessage("bad element type, expected " + type + " but was " + element.getClass())
                    .withDataLocation(dataLocation)
                    .build();
            }

            return extractor.extract(dataLocation, type.cast(element));
        }
    }
}
