package org.phantazm.loader;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
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

    static <T> @NotNull Loader<T> loader(@NotNull Supplier<? extends @NotNull DataSource> dataSourceSupplier,
        @NotNull ObjectExtractor<T> extractor) {
        Objects.requireNonNull(dataSourceSupplier);
        Objects.requireNonNull(extractor);
        return new Impl<>(dataSourceSupplier, extractor);
    }

    class Impl<T> implements Loader<T> {
        private final Supplier<? extends DataSource> dataSourceSupplier;
        private final ObjectExtractor<T> extractor;

        private Map<Key, T> data;

        private Impl(Supplier<? extends @NotNull DataSource> dataSourceSupplier, ObjectExtractor<T> extractor) {
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

                        handler.run(() -> {
                            if (!element.isContainer()) {
                                throw LoaderException.builder()
                                    .withElement(element)
                                    .withMessage("expected element to be a container")
                                    .build();
                            }

                            ConfigContainer container = element.asContainer();

                            Collection<ObjectExtractor.Entry<T>> entries;
                            try {
                                entries = extractor.extract(container);
                            } catch (ElementException e) {
                                throw LoaderException.builder()
                                    .withElement(container)
                                    .withMessage("exception when loading from container")
                                    .withCause(e)
                                    .build();
                            }

                            for (ObjectExtractor.Entry<T> entry : entries) {
                                if (map.putIfAbsent(entry.identifier(), entry.object()) != null) {
                                    throw LoaderException.builder()
                                        .withElement(container)
                                        .withMessage("duplicate key " + entry.identifier())
                                        .build();
                                }
                            }
                        });
                    }
                }
            }

            this.data = Map.copyOf(map);
        }
    }
}
