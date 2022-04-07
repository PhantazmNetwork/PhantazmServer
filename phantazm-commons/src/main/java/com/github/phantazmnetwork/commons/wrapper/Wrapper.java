package com.github.phantazmnetwork.commons.wrapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * <p>A simple container object which may contain some value, or {@code null}. This is primarily useful when attempting
 * to pass values outside the scope of a lambda expression, or to simulate pass-by-reference semantics.</p>
 *
 * <p>Wrapper instances are considered equal if and only if the objects they contain are equal. Likewise, a wrapper's
 * hashcode is equal to the hashcode of the stored object, or 0 if {@code null}.</p>
 * @param <TType> the type of object stored in this reference
 */
public abstract class Wrapper<TType> implements Supplier<TType> {
    private static final class ObjectWrapper<T> extends Wrapper<T> {
        private T value;

        private ObjectWrapper(T value) {
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

    //disallow subclassing outside package
    Wrapper() {}

    /**
     * Sets the value stored by this wrapper.
     * @param value the new value
     */
    public abstract void set(@Nullable TType value);

    @Override
    public int hashCode() {
        return Objects.hashCode(get());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) {
            return false;
        }

        if(obj == this) {
            return true;
        }

        if(obj instanceof Wrapper<?> other) {
            return Objects.equals(get(), other.get());
        }

        return false;
    }

    @Override
    public String toString() {
        return "Wrapper{value=" + get() + "}";
    }

    /**
     * Creates and returns a new Wrapper implementation. For primitive values, instead consider using the appropriate
     * primitive specialization of this method.
     * @param value the value to initially store in the wrapper
     * @param <T> the type of value held in the wrapper
     * @return a mutable Wrapper implementation
     */
    public static <T> @NotNull Wrapper<T> of(@Nullable T value) {
        return new ObjectWrapper<>(value);
    }

    /**
     * Creates and returns a new IntWrapper implementation. This is the primitive type specialization of Wrapper for
     * {@code int}.
     * @param value the initial value to store in the wrapper
     * @return a new primitive wrapper
     */
    public static @NotNull IntWrapper ofInt(int value) {
        return new IntWrapper(value);
    }

    /**
     * Converts this Wrapper to an Optional instance.
     * @return an Optional instance containing the same value as this wrapper, or {@link Optional#empty()} if this
     * wrapper contains null
     */
    public @NotNull Optional<TType> toOptional() {
        return Optional.ofNullable(get());
    }

    /**
     * Applies a mapping function to this wrapper's value, and returns a new wrapper containing the result of this
     * function.
     * @param mapper the mapping function to apply
     * @param <TNew> the new value
     * @return a new wrapper holding the mapped value
     */
    public <TNew> @NotNull Wrapper<TNew> map(@NotNull Function<? super TType, ? extends TNew> mapper) {
        return new ObjectWrapper<>(mapper.apply(get()));
    }

    /**
     * Applies a mapping function to this wrapper's value. The result of the mapping function will be the new value
     * held by this wrapper.
     * @param mapper the mapping function to apply
     */
    public void apply(@NotNull Function<? super TType, ? extends TType> mapper) {
        set(mapper.apply(get()));
    }
}