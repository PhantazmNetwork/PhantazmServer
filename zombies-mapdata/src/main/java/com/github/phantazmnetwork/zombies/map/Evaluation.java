package com.github.phantazmnetwork.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Predicate;

public enum Evaluation {
    ALL_TRUE,
    ANY_TRUE;

    public <T> boolean evaluate(@NotNull Iterable<? extends Predicate<T>> predicates, T type) {
        Objects.requireNonNull(predicates, "predicates");
        
        return switch (this) {
            case ALL_TRUE -> {
                for (Predicate<T> predicate : predicates) {
                    if (!predicate.test(type)) {
                        yield false;
                    }
                }

                yield true;
            }
            case ANY_TRUE -> {
                for (Predicate<T> predicate : predicates) {
                    if (predicate.test(type)) {
                        yield true;
                    }
                }

                yield false;
            }
        };
    }
}
