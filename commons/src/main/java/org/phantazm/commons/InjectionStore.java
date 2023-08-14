package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public sealed interface InjectionStore permits InjectionStore.InjectionStoreImpl {
    InjectionStore EMPTY = new InjectionStoreImpl(Map.of());

    <T> @NotNull T get(@NotNull Key<T> key);

    <T> boolean has(@NotNull Key<T> key);

    @NotNull @Unmodifiable Set<@NotNull Key<?>> keys();

    @NotNull @Unmodifiable Collection<@NotNull Object> objects();

    sealed interface Builder permits BuilderImpl {
        <T> @NotNull Builder with(@NotNull Key<T> key, @NotNull T object);

        @NotNull InjectionStore build();
    }

    sealed interface Key<T> permits InjectionStore.KeyImpl {
        @NotNull Class<T> type();

        @NotNull String id();
    }

    static @NotNull Builder builder() {
        return new BuilderImpl();
    }

    static @NotNull <T> Key<T> key(@NotNull Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz");
        return new KeyImpl<>(clazz, "");
    }

    static @NotNull <T> Key<T> key(@NotNull Class<T> clazz, @NotNull String id) {
        Objects.requireNonNull(clazz, "clazz");
        Objects.requireNonNull(id, "id");
        return new KeyImpl<>(clazz, id);
    }

    static @NotNull <T> InjectionStore of(@NotNull Key<T> key, @NotNull T object) {
        return new InjectionStoreImpl(Map.of(key, object));
    }

    static @NotNull <T, V> InjectionStore of(@NotNull Key<T> key1, @NotNull T object1, @NotNull Key<V> key2,
            @NotNull V object2) {
        return new InjectionStoreImpl(Map.of(key1, object1, key2, object2));
    }

    final class BuilderImpl implements Builder {
        private final Map<Key<?>, Object> values;

        private BuilderImpl() {
            this.values = new HashMap<>();
        }

        @Override
        public @NotNull <T> Builder with(@NotNull Key<T> key, @NotNull T object) {
            Objects.requireNonNull(key, "key");
            Objects.requireNonNull(object, "object");
            values.put(key, object);
            return this;
        }

        @Override
        public @NotNull InjectionStore build() {
            return new InjectionStoreImpl(Map.copyOf(values));
        }
    }

    final class KeyImpl<T> implements Key<T> {
        private final Class<T> clazz;
        private final String id;

        private KeyImpl(Class<T> clazz, String id) {
            this.clazz = clazz;
            this.id = id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, id);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            if (obj instanceof KeyImpl<?> other) {
                return clazz.equals(other.clazz) && id.equals(other.id);
            }

            return false;
        }

        @Override
        public String toString() {
            return "Key[" + clazz.getSimpleName() + (id.isEmpty() ? "]" : (", " + id + "]"));
        }

        @Override
        public @NotNull Class<T> type() {
            return clazz;
        }

        @Override
        public @NotNull String id() {
            return id;
        }
    }

    @SuppressWarnings("unchecked")
    final class InjectionStoreImpl implements InjectionStore {
        private final Map<Key<?>, Object> mappings;

        private InjectionStoreImpl(@NotNull Map<Key<?>, Object> mappings) {
            this.mappings = mappings;
        }

        @Override
        public <T> @NotNull T get(@NotNull Key<T> key) {
            T object = (T)mappings.get(key);
            if (object == null) {
                throw new IllegalArgumentException(key + " not present in store");
            }

            return object;
        }

        @Override
        public <T> boolean has(@NotNull Key<T> key) {
            return mappings.containsKey(key);
        }

        @Override
        public @NotNull @Unmodifiable Set<Key<?>> keys() {
            return mappings.keySet();
        }

        @Override
        public @NotNull @Unmodifiable Collection<Object> objects() {
            return mappings.values();
        }
    }
}
