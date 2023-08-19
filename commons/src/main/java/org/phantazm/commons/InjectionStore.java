package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public sealed interface InjectionStore permits InjectionStore.InjectionStoreImpl {
    InjectionStore EMPTY = new InjectionStoreImpl(Map.of());

    static @NotNull Builder builder() {
        return new BuilderImpl();
    }

    static @NotNull <T> Key<T> key(@NotNull Class<T> clazz) {
        Objects.requireNonNull(clazz);
        return new KeyImpl<>(clazz, "");
    }

    static @NotNull <T> Key<T> key(@NotNull Class<T> clazz, @NotNull String id) {
        Objects.requireNonNull(clazz);
        Objects.requireNonNull(id);
        return new KeyImpl<>(clazz, id);
    }

    static @NotNull InjectionStore of() {
        return EMPTY;
    }

    static @NotNull <T> InjectionStore of(@NotNull Key<T> key, @NotNull T object) {
        InjectionStoreImpl.validateEntry(key, object);
        return new InjectionStoreImpl(Map.of(key, object));
    }

    static @NotNull <T, V> InjectionStore of(@NotNull Key<T> key1, @NotNull T object1, @NotNull Key<V> key2,
        @NotNull V object2) {
        InjectionStoreImpl.validateEntry(key1, object1);
        InjectionStoreImpl.validateEntry(key2, object2);
        return new InjectionStoreImpl(Map.of(key1, object1, key2, object2));
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    static @NotNull InjectionStore ofEntries(@NotNull Map.Entry<Key<?>, Object> @NotNull ... entries) {
        if (entries.length == 0) {
            return EMPTY;
        }

        if (entries.length == 1) {
            Map.Entry<Key<?>, Object> entry = entries[0];

            Key<?> key = entry.getKey();
            Object value = entry.getValue();
            InjectionStoreImpl.validateEntry(key, value);
            return new InjectionStoreImpl(Map.of(key, value));
        }

        Map.Entry<Key<?>, Object>[] newEntries = new Map.Entry[entries.length];
        for (int i = 0; i < newEntries.length; i++) {
            Map.Entry<Key<?>, Object> entry = entries[i];
            Key<?> key = entry.getKey();
            Object value = entry.getValue();

            InjectionStoreImpl.validateEntry(key, value);
            newEntries[i] = Map.entry(key, value);
        }

        return new InjectionStoreImpl(Map.ofEntries(newEntries));
    }

    <T> @NotNull T get(@NotNull Key<T> key);

    <T> boolean has(@NotNull Key<T> key);

    @NotNull
    @Unmodifiable
    Set<@NotNull Key<?>> keys();

    @NotNull
    @Unmodifiable
    Collection<@NotNull Object> objects();

    sealed interface Builder permits BuilderImpl {
        <T> @NotNull Builder with(@NotNull Key<T> key, @NotNull T object);

        @NotNull
        InjectionStore build();
    }

    sealed interface Key<T> permits InjectionStore.KeyImpl {
        @NotNull
        Class<T> type();

        @NotNull
        String id();
    }

    final class BuilderImpl implements Builder {
        private final Map<Key<?>, Object> values;

        private BuilderImpl() {
            this.values = new HashMap<>();
        }

        @Override
        public @NotNull <T> Builder with(@NotNull Key<T> key, @NotNull T object) {
            InjectionStoreImpl.validateEntry(key, object);

            values.put(key, object);
            return this;
        }

        @Override
        public @NotNull InjectionStore build() {
            if (values.isEmpty()) {
                return EMPTY;
            }

            return new InjectionStoreImpl(Map.copyOf(values));
        }
    }

    final class KeyImpl<T> implements Key<T> {
        private final Class<T> clazz;
        private final String id;

        private int hash;
        private boolean hashed;

        private KeyImpl(Class<T> clazz, String id) {
            this.clazz = clazz;
            this.id = id;
        }

        @Override
        public int hashCode() {
            if (hashed) {
                return hash;
            }

            hash = Objects.hash(clazz, id);
            hashed = true;
            return hash;
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

        private static void validateEntry(Key<?> key, Object object) {
            Objects.requireNonNull(key);
            Objects.requireNonNull(object);

            Class<?> objectClass = object.getClass();
            if (!key.getClass().isAssignableFrom(objectClass)) {
                throw new IllegalArgumentException("bad type " + objectClass + " not assignable to " + key);
            }
        }

        @Override
        public <T> @NotNull T get(@NotNull Key<T> key) {
            T object = (T) mappings.get(key);
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
        public @NotNull
        @Unmodifiable Set<Key<?>> keys() {
            return mappings.keySet();
        }

        @Override
        public @NotNull
        @Unmodifiable Collection<Object> objects() {
            return mappings.values();
        }
    }
}
