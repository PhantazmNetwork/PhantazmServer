package com.github.phantazmnetwork.commons.wrapper;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.IntSupplier;

/**
 * Primitive specialization of {@link Wrapper} for {@code int}. Instances may be obtained by calling
 * {@link Wrapper#ofInt(int)}.
 */
public final class IntWrapper extends Wrapper<Integer> implements IntSupplier {
    private int value;

    /**
     * Constructs a new IntWrapper.
     * @param value the initial value of this wrapper
     */
    IntWrapper(int value) {
        this.value = value;
    }

    @Override
    public void set(@Nullable Integer value) {
        this.value = value == null ? 0 : value;
    }

    /**
     * Type specialization of {@link IntWrapper#set(Integer)} to avoid unboxing.
     * @param value the value to set
     */
    public void setAsInt(int value) {
        this.value = value;
    }

    @Override
    public int getAsInt() {
        return value;
    }

    @Override
    public Integer get() {
        return value;
    }

    /**
     * Primitive specialization of {@link Wrapper#apply(Function)}.
     * @param mapper the mapping function to apply to the value
     */
    public void apply(@NotNull Int2IntFunction mapper) {
        value = mapper.apply(value);
    }
}
