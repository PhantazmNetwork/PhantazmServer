package org.phantazm.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.toolkit.function.ExceptionHandler;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Supplier;

public interface Loader<T> {
    @NotNull @Unmodifiable Collection<T> anonymousData();

    @NotNull @Unmodifiable Map<Key, T> data();

    void load() throws IOException;

    default void loadUnchecked() {
        try {
            load();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static <T, V extends ConfigElement> @NotNull Loader<T> loader(
        @NotNull Supplier<? extends @NotNull DataSource> dataSourceSupplier,
        @NotNull ObjectExtractor<T, V> extractor) {
        Objects.requireNonNull(dataSourceSupplier);
        Objects.requireNonNull(extractor);
        return new Impl<>(dataSourceSupplier, extractor, false);
    }

    static <T, V extends ConfigElement> @NotNull Loader<T> anonymousLoader(
        @NotNull Supplier<? extends @NotNull DataSource> dataSourceSupplier,
        @NotNull ObjectExtractor<T, V> extractor) {
        Objects.requireNonNull(dataSourceSupplier);
        Objects.requireNonNull(extractor);
        return new Impl<>(dataSourceSupplier, extractor, true);
    }

    class Impl<T, V extends ConfigElement> implements Loader<T> {
        private final Supplier<? extends DataSource> dataSourceSupplier;
        private final ObjectExtractor<T, V> extractor;
        private final boolean anonymous;

        private Map<Key, T> data;
        private Collection<T> anonymousData;

        private Impl(Supplier<? extends DataSource> dataSourceSupplier, ObjectExtractor<T, V> extractor,
            boolean anonymous) {
            this.dataSourceSupplier = dataSourceSupplier;
            this.extractor = extractor;
            this.anonymous = anonymous;
        }

        private Map<Key, T> requireData() {
            Map<Key, T> data = this.data;
            if (data == null) {
                if (anonymousData != null) {
                    throw new IllegalStateException("loader is anonymous");
                }

                throw new IllegalStateException("data has not been loaded yet");
            }

            return data;
        }

        private Collection<T> requireAnonymousData() {
            Collection<T> data = this.anonymousData;
            if (data == null) {
                throw new IllegalStateException("anonymous data has not been loaded yet");
            }

            return data;
        }

        @Override
        public @NotNull @Unmodifiable Collection<T> anonymousData() {
            return requireAnonymousData();
        }

        @Override
        public @NotNull @Unmodifiable Map<Key, T> data() {
            return requireData();
        }

        @Override
        public void load() throws IOException {
            Map<Key, T> map = anonymous ? null : new HashMap<>();
            List<T> list = anonymous ? new ArrayList<>() : null;

            try (ExceptionHandler<IOException> handler = new ExceptionHandler<>(IOException.class);
                 DataSource dataSource = dataSourceSupplier.get()) {
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
                                .withMessage("exception when loading data from configuration")
                                .withDataLocation(location)
                                .withCause(exception)
                                .build();
                        }

                        for (ObjectExtractor.Entry<T> entry : entries) {
                            if (anonymous) {
                                list.add(entry.object());
                                continue;
                            }

                            Key identifier = entry.identifier();
                            if (identifier == null) {
                                throw LoaderException.builder()
                                    .withElement(element)
                                    .withMessage("got an anonymous entry for a non-anonymous loader")
                                    .withDataLocation(location)
                                    .build();
                            }

                            if (map.putIfAbsent(identifier, entry.object()) != null) {
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

            if (anonymous) {
                this.anonymousData = List.copyOf(list);
                return;
            }

            Map<Key, T> data = Map.copyOf(map);
            this.data = data;
            this.anonymousData = data.values();
        }

        private static <T, V extends ConfigElement> Collection<ObjectExtractor.Entry<T>> extract(
            DataLocation dataLocation, ObjectExtractor<T, V> extractor, ConfigElement element) throws IOException {
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
