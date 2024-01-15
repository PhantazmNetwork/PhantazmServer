package org.phantazm.loader;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.toolkit.function.ExceptionHandler;
import com.github.steanky.toolkit.function.ThrowingFunction;
import com.github.steanky.toolkit.function.ThrowingSupplier;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

public interface Loader<T> {
    interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

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

    default T first() {
        return anonymousData().iterator().next();
    }

    default @NotNull <V> Loader<V> mergingMap(
        @NotNull ThrowingFunction<? super Map<Key, T>, ? extends V, ? extends IOException> mergeFunction) {
        return mergingMap(mergeFunction, null);
    }

    default @NotNull <V> Loader<V> mergingMap(
        @NotNull ThrowingFunction<? super Map<Key, T>, ? extends V, ? extends IOException> mergeFunction, @Nullable String stage) {
        return new AbstractLoader<>() {
            @Override
            public void load() throws IOException {
                Loader.this.load();
                Map<Key, T> data = Loader.this.data();

                try {
                    super.anonymousData = List.of(mergeFunction.apply(data));
                } catch (IOException e) {
                    if (e instanceof LoaderException loaderException) {
                        throw loaderException.toBuilder().withStage(stage).build();
                    }

                    throw LoaderException.builder().withStage(stage).withCause(e).build();
                }
            }
        };
    }

    default @NotNull <V> Loader<V> transforming(
        @NotNull ThrowingFunction<? super T, ? extends V, ? extends IOException> transformationFunction) {
        return transforming(transformationFunction, null);
    }

    default @NotNull <V> Loader<V> transforming(
        @NotNull ThrowingFunction<? super T, ? extends V, ? extends IOException> transformationFunction,
        @Nullable String stage) {
        Objects.requireNonNull(transformationFunction);

        return new AbstractLoader<>() {
            @SuppressWarnings("unchecked")
            @Override
            public void load() throws IOException {
                Loader.this.load();

                Map<Key, T> data = Loader.this.data();

                try {
                    Map.Entry<Key, V>[] entries = new Map.Entry[data.size()];
                    int i = 0;
                    for (Map.Entry<Key, T> entry : data.entrySet()) {
                        entries[i++] = Map.entry(entry.getKey(), transformationFunction.apply(entry.getValue()));
                    }

                    this.data = Map.ofEntries(entries);
                } catch (IOException e) {
                    if (e instanceof LoaderException loaderException) {
                        throw loaderException.toBuilder().withStage(stage).build();
                    }

                    throw LoaderException.builder().withStage(stage).withCause(e).build();
                }
            }
        };
    }

    default @NotNull Loader<T> accepting(@NotNull ThrowingConsumer<? super Collection<T>, ? extends IOException> consumer) {
        return accepting(consumer, null);
    }

    default @NotNull Loader<T> accepting(@NotNull ThrowingConsumer<? super Collection<T>, ? extends IOException> consumer,
        @Nullable String stage) {
        return new Loader<>() {
            @Override
            public @NotNull @Unmodifiable Collection<T> anonymousData() {
                return Loader.this.anonymousData();
            }

            @Override
            public @NotNull @Unmodifiable Map<Key, T> data() {
                return Loader.this.data();
            }

            @Override
            public void load() throws IOException {
                Loader.this.load();

                try {
                    consumer.accept(Loader.this.anonymousData());
                } catch (IOException e) {
                    if (e instanceof LoaderException loaderException) {
                        throw loaderException.toBuilder().withStage(stage).build();
                    }

                    throw LoaderException.builder().withStage(stage).withCause(e).build();
                }
            }
        };
    }

    static <T, V extends ConfigElement> @NotNull Loader<T> loader(
        @NotNull ThrowingSupplier<? extends @NotNull DataSource, ? extends IOException> dataSourceSupplier,
        @NotNull ObjectExtractor<T, V> extractor) {
        Objects.requireNonNull(dataSourceSupplier);
        Objects.requireNonNull(extractor);
        return new Impl<>(dataSourceSupplier, extractor, false);
    }

    static <T, V extends ConfigElement> @NotNull Loader<T> anonymousLoader(
        @NotNull ThrowingSupplier<? extends DataSource, ? extends IOException> dataSourceSupplier,
        @NotNull ObjectExtractor<T, V> extractor) {
        Objects.requireNonNull(dataSourceSupplier);
        Objects.requireNonNull(extractor);
        return new Impl<>(dataSourceSupplier, extractor, true);
    }

    abstract class AbstractLoader<T> implements Loader<T> {
        protected Map<Key, T> data;
        protected Collection<T> anonymousData;

        @Override
        public @NotNull @Unmodifiable Collection<T> anonymousData() {
            Collection<T> data = this.anonymousData;
            if (data == null) {
                throw new IllegalStateException("anonymous data has not been loaded yet");
            }

            return data;
        }

        @Override
        public @NotNull @Unmodifiable Map<Key, T> data() {
            Map<Key, T> data = this.data;
            if (data == null) {
                if (anonymousData != null) {
                    throw new IllegalStateException("loader is anonymous");
                }

                throw new IllegalStateException("data has not been loaded yet");
            }

            return data;
        }
    }

    class Impl<T, V extends ConfigElement> extends AbstractLoader<T> {
        private final ThrowingSupplier<? extends DataSource, ? extends IOException> dataSourceSupplier;
        private final ObjectExtractor<T, V> extractor;
        private final boolean anonymous;

        private Impl(ThrowingSupplier<? extends DataSource, ? extends IOException> dataSourceSupplier, ObjectExtractor<T, V> extractor,
            boolean anonymous) {
            this.dataSourceSupplier = dataSourceSupplier;
            this.extractor = extractor;
            this.anonymous = anonymous;
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
                                    .withMessage("found anonymous entry for non-anonymous loader")
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
