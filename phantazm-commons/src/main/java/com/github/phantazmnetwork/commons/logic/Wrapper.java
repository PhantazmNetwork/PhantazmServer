package com.github.phantazmnetwork.commons.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * A simple container object which may contain some value, or {@code null}. Useful when lambda expressions need to pass
 * data outside their scope through captured variables.
 * @param <T> the type of object stored in this reference
 */
public interface Wrapper<T> extends Supplier<T> {
    @SuppressWarnings("ClassCanBeRecord")
    class Immutable<T> implements Wrapper<T> {
        private final T value;

        private Immutable(T value) {
            this.value = value;
        }

        @Override
        public void set(@Nullable T value) {
            throwSetException();
        }

        @Override
        public T get() {
            return value;
        }

        private static void throwSetException() {
            throw new UnsupportedOperationException("This reference is immutable");
        }
    }

    class Mutable<T> implements Wrapper<T> {
        private T value;

        private Mutable(T value) {
            this.value = value;
        }

        @Override
        public void set(@Nullable T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }
    }

    /**
     * Sets the value stored in this wrapper.
     * @param value the new value
     * @throws UnsupportedOperationException if this implementation is immutable
     */
    void set(@Nullable T value);

    /**
     * The immutable null wrapper, used by {@link Wrapper#nullWrapper()}.
     */
    Wrapper<?> NULL = new Wrapper<>() {
        @Override
        public void set(@Nullable Object value) {
            Immutable.throwSetException();
        }

        @Override
        public Object get() {
            return null;
        }
    };

    static <T> Wrapper<T> mutable(@Nullable T value) {
        return new Mutable<>(value);
    };

    static <T> Wrapper<T> immutable(@Nullable T value) {
        if(value == null) {
            return nullWrapper();
        }

        return new Immutable<>(value);
    }

    /**
     * Returns the shared, immutable null wrapper cast to the specified type.
     * @param <T> the type of reference held by this instance
     * @return the null reference, after casting
     */
    static <T> @NotNull Wrapper<T> nullWrapper() {
        //noinspection unchecked
        return (Wrapper<T>)NULL;
    }
}
