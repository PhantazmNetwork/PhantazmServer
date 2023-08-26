package org.phantazm.zombies.map;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiPredicate;

public enum Evaluation {
    ALL_TRUE,
    ANY_TRUE;

    public <T, V> boolean evaluate(@NotNull Iterable<? extends BiPredicate<T, V>> predicates, T type, V second) {
        Objects.requireNonNull(predicates);

        return switch (this) {
            case ALL_TRUE -> {
                for (BiPredicate<T, V> predicate : predicates) {
                    if (!predicate.test(type, second)) {
                        yield false;
                    }
                }

                yield true;
            }
            case ANY_TRUE -> {
                for (BiPredicate<T, V> predicate : predicates) {
                    if (predicate.test(type, second)) {
                        yield true;
                    }
                }

                yield false;
            }
        };
    }
}
