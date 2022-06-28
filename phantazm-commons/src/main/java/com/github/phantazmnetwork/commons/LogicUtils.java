package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * Utilities for controlling program flow and logic.
 */
public final class LogicUtils {
    private LogicUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * If the input object {@code in} is null, returns null. Otherwise, calls the mapping function with its value and
     * returns the result of that function.
     * @param in the input object
     * @param mapper the mapping function
     * @return null if in is null, or the result of calling the specified mapping function
     * @param <TIn> the input type
     * @param <TOut> the output type
     */
    public static <TIn, TOut> @Nullable TOut nullCoalesce(@Nullable TIn in,
                                                          @NotNull Function<? super TIn, ? extends TOut> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        if(in == null) {
            return null;
        }

        return mapper.apply(in);
    }
}
