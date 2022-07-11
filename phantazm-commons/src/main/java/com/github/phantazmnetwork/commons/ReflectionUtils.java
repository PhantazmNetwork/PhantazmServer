package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.function.Predicate;

public final class ReflectionUtils {
    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }

    public static @Nullable Method declaredMethodMatching(@NotNull Class<?> inspect,
                                                          @NotNull Predicate<? super Method> predicate) {
        for(Method method : inspect.getDeclaredMethods()) {
            if(predicate.test(method)) {
                return method;
            }
        }

        return null;
    }
}
