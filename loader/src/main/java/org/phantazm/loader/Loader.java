package org.phantazm.loader;

import com.github.steanky.element.core.ElementException;
import com.github.steanky.element.core.context.ContextManager;
import com.github.steanky.element.core.context.ElementContext;
import com.github.steanky.element.core.dependency.DependencyProvider;
import com.github.steanky.element.core.key.KeyExtractor;
import com.github.steanky.element.core.path.ElementPath;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.collection.ConfigContainer;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public interface Loader<T> {
    @NotNull @Unmodifiable Map<Key, T> data();

    void load() throws IOException;

    static <T> @NotNull Loader<T> loader(@NotNull Class<T> type, @NotNull KeyExtractor keyExtractor,
        @NotNull Supplier<? extends @NotNull DataSource> dataSourceSupplier, @NotNull ContextManager contextManager,
        @NotNull PathExtractor pathExtractor) {
        return loader(type, keyExtractor, dataSourceSupplier, contextManager, pathExtractor, DependencyProvider.EMPTY);
    }

    static <T> @NotNull Loader<T> loader(@NotNull Class<T> type, @NotNull KeyExtractor keyExtractor,
        @NotNull Supplier<? extends @NotNull DataSource> dataSourceSupplier, @NotNull ContextManager contextManager,
        @NotNull PathExtractor pathExtractor, @NotNull DependencyProvider dependencyProvider) {
        Objects.requireNonNull(type);
        Objects.requireNonNull(keyExtractor);
        Objects.requireNonNull(dataSourceSupplier);
        Objects.requireNonNull(contextManager);
        Objects.requireNonNull(pathExtractor);
        Objects.requireNonNull(dependencyProvider);

        return new Impl<>(type, keyExtractor, dataSourceSupplier, contextManager, pathExtractor,
            dependencyProvider);
    }

    class Impl<T> implements Loader<T> {
        private final Class<T> type;
        private final KeyExtractor keyExtractor;
        private final Supplier<? extends DataSource> dataSourceSupplier;
        private final ContextManager contextManager;
        private final PathExtractor pathExtractor;
        private final DependencyProvider dependencyProvider;

        private Map<Key, T> data;

        private Impl(Class<T> type, KeyExtractor keyExtractor,
            Supplier<? extends @NotNull DataSource> dataSourceSupplier, ContextManager contextManager,
            PathExtractor pathExtractor, DependencyProvider dependencyProvider) {
            this.type = type;
            this.keyExtractor = keyExtractor;
            this.dataSourceSupplier = dataSourceSupplier;
            this.contextManager = contextManager;
            this.pathExtractor = pathExtractor;
            this.dependencyProvider = dependencyProvider;
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

            IOException exception = null;
            try (DataSource dataSource = dataSourceSupplier.get()) {
                while (dataSource.hasNext()) {
                    ConfigElement element = dataSource.next();

                    try {
                        if (!element.isContainer()) {
                            throw FileLoaderException.builder()
                                .withElement(element)
                                .withMessage("expected element to be a container")
                                .build();
                        }

                        ConfigContainer container = element.asContainer();
                        ElementContext context = contextManager.makeContext(container);

                        List<ElementPath> paths = pathExtractor.paths(container);
                        for (ElementPath path : paths) {
                            Object result;
                            try {
                                result = context.provide(path, dependencyProvider, false,
                                    ElementContext.DEFAULT_EXCEPTION_HANDLER, () -> null);
                            } catch (ElementException e) {
                                throw FileLoaderException.builder()
                                    .withElement(element)
                                    .withElementPath(path)
                                    .withCause(e)
                                    .build();
                            }

                            if (!type.isAssignableFrom(result.getClass())) {
                                throw FileLoaderException.builder()
                                    .withElement(container)
                                    .withElementPath(path)
                                    .withMessage("bad type of provided object; must be assignable to " + type)
                                    .build();
                            }

                            Key key;
                            try {
                                key = keyExtractor.extractKey(path.followNode(container));
                            } catch (ElementException e) {
                                throw FileLoaderException.builder()
                                    .withElement(container)
                                    .withElementPath(path)
                                    .withCause(e)
                                    .build();
                            }

                            if (map.putIfAbsent(key, type.cast(result)) != null) {
                                throw FileLoaderException.builder()
                                    .withElement(container)
                                    .withElementPath(path)
                                    .withMessage("key " + key + " already exists")
                                    .build();
                            }
                        }
                    } catch (IOException e) {
                        if (exception == null) {
                            exception = e;
                        } else {
                            exception.addSuppressed(e);
                        }
                    }
                }
            }

            this.data = Map.copyOf(map);
        }
    }
}
