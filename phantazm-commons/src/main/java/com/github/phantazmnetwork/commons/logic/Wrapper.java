package com.github.phantazmnetwork.commons.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A simple container object which may contain some value, or {@code null}. The mutable implementation is useful when
 * lambda expressions need to pass data outside their scope through captured variables. Either may be used in cases
 * where it's necessary to distinguish between absence of a value and a null value, for example as a value stored in a
 * map.
 * @param <T> the type of object stored in this reference
 */
public abstract class Wrapper<T> implements Supplier<T> {
    private static final class Immutable<T> extends Wrapper<T> {
        private final T value;

        private Immutable(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public String toString() {
            return "Wrapper.Immutable{value=" + value + "}";
        }
    }

    private static final class Mutable<T> extends Wrapper<T> {
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

        @Override
        public String toString() {
            return "Wrapper.Mutable{value=" + value + "}";
        }
    }

    private static final Wrapper<?> NULL = new Wrapper<>() {
        @Override
        public Object get() {
            return null;
        }

        @Override
        public String toString() {
            return "Wrapper.Immutable{value=null}";
        }
    };

    //disallow subclassing anywhere else
    private Wrapper() {}

    /**
     * Sets the value stored in this wrapper. Throws an {@link UnsupportedOperationException} by default.
     * @param value the new value
     * @throws UnsupportedOperationException if this implementation is immutable
     */
    void set(@Nullable T value) {
        throw new UnsupportedOperationException("This Wrapper is immutable");
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(get());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj instanceof Wrapper<?> other) {
            return Objects.equals(get(), other.get());
        }

        return false;
    }

    /**
     * Creates and returns a new, mutable Wrapper implementation.
     * @param value the value to initially store in the wrapper
     * @param <T> the type of value held in the wrapper
     * @return a mutable Wrapper implementation
     */
    public static <T> @NotNull Wrapper<T> mutable(@Nullable T value) {
        return new Mutable<>(value);
    }

    /**
     * Creates and returns an immutable Wrapper implementation. If the given value is null, the common immutable null
     * wrapper will be returned.
     * @param value the value stored in this wrapper
     * @param <T> the type of object held in this wrapper
     * @return a new immutable wrapper implementation if value is non-null, otherwise the shared null instance
     */
    public static <T> @NotNull Wrapper<T> immutable(@Nullable T value) {
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
    public static <T> @NotNull Wrapper<T> nullWrapper() {
        //noinspection unchecked
        return (Wrapper<T>) NULL;
    }
}
