package com.github.phantazmnetwork.commons.iterator;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An extension of {@link Iterator} that provides some additional useful features in the functional programming style.
 * @param <TValue> the value returned by {@link Iterator#next()}
 */
public interface EnhancedIterator<TValue> extends Iterator<TValue> {
    /**
     * The shared "empty" iterator whose {@link Iterator#hasNext()} method always returns false. {@link Iterator#next()}
     * will always throw a {@link NoSuchElementException}.
     */
    EnhancedIterator<?> EMPTY = new EnhancedIterator<>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    };

    default <TOut> @NotNull EnhancedIterator<TOut> map(@NotNull Function<? super TValue, ? extends TOut> mapper) {
        Objects.requireNonNull(mapper, "mapper");
        return new EnhancedIterator<>() {
            @Override
            public boolean hasNext() {
                return EnhancedIterator.this.hasNext();
            }

            @Override
            public TOut next() {
                return mapper.apply(EnhancedIterator.this.next());
            }
        };
    }

    default <TOut> @NotNull EnhancedIterator<TOut> flatMap(@NotNull Function<? super TValue,
            ? extends Iterator<? extends TOut>> mapper) {
        Objects.requireNonNull(mapper, "mapper");

        return new AdvancingIterator<>() {
            private Iterator<? extends TOut> itr;

            @Override
            public boolean advance() {
                while(itr == null || !itr.hasNext()) {
                    if(!EnhancedIterator.this.hasNext()) {
                        return false;
                    }

                    itr = mapper.apply(EnhancedIterator.this.next());
                }

                this.value = itr.next();
                return true;
            }
        };
    }

    default @NotNull EnhancedIterator<TValue> filter(@NotNull Predicate<? super TValue> filter) {
        Objects.requireNonNull(filter, "filter");

        return new AdvancingIterator<>() {
            @Override
            public boolean advance() {
                while(EnhancedIterator.this.hasNext()) {
                    TValue value = EnhancedIterator.this.next();
                    if(filter.test(value)) {
                        this.value = value;
                        return true;
                    }
                }

                return false;
            }
        };
    }

    default @NotNull EnhancedIterator<TValue> until(@NotNull Predicate<? super TValue> condition) {
        Objects.requireNonNull(condition, "condition");
        return new AdvancingIterator<>() {
            @Override
            public boolean advance() {
                if(!EnhancedIterator.this.hasNext()) {
                    return false;
                }

                TValue value = EnhancedIterator.this.next();
                if(condition.test(value)) {
                    return false;
                }

                this.value = value;
                return true;
            }
        };
    }

    default @NotNull EnhancedIterator<TValue> listen(@NotNull Consumer<? super TValue> listener) {
        Objects.requireNonNull(listener, "listener");
        return new EnhancedIterator<>() {
            @Override
            public boolean hasNext() {
                return EnhancedIterator.this.hasNext();
            }

            @Override
            public TValue next() {
                TValue value = EnhancedIterator.this.next();
                listener.accept(value);
                return value;
            }
        };
    }

    default @NotNull EnhancedIterator<TValue> after(@NotNull Runnable action) {
        return new EnhancedIterator<>() {
            boolean finished = false;

            @Override
            public boolean hasNext() {
                boolean hasNext = EnhancedIterator.this.hasNext();
                if(!hasNext && !finished) {
                    action.run();
                    finished = true;
                }
                return hasNext;
            }

            @Override
            public TValue next() {
                return EnhancedIterator.this.next();
            }
        };
    }

    default void drain(@NotNull Consumer<? super TValue> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        while(hasNext()) {
            consumer.accept(next());
        }
    }

    default void drain(@NotNull Collection<? super TValue> receiver) {
        Objects.requireNonNull(receiver, "receiver");
        while(hasNext()) {
            receiver.add(next());
        }
    }

    default void drain() {
        while(hasNext()) {
            next();
        }
    }

    default @NotNull EnhancedIterator<TValue> synchronize() {
        if(this instanceof SynchronizedIterator<?> iterator) {
            //noinspection unchecked
            return (EnhancedIterator<TValue>) iterator;
        }

        return new SynchronizedIterator<>(this);
    }

    static <TValue> @NotNull EnhancedIterator<TValue> adapt(@NotNull Iterator<? extends TValue> iterator) {
        Objects.requireNonNull(iterator, "iterator");
        if(iterator instanceof EnhancedIterator<?> enhanced) {
            //noinspection unchecked
            return (EnhancedIterator<TValue>) enhanced;
        }

        return new EnhancedIterator<>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public TValue next() {
                return iterator.next();
            }
        };
    }

    @SafeVarargs
    static <TValue> @NotNull EnhancedIterator<TValue> of(TValue... elements) {
        if(elements == null) {
            return new SingletonIterator<>(null);
        }

        if(elements.length == 0) {
            return empty();
        }
        else if(elements.length == 1) {
            return new SingletonIterator<>(elements[0]);
        }
        else {
            return new ArrayIterator<>(elements);
        }
    }

    static <TValue> @NotNull EnhancedIterator<TValue> empty() {
        //noinspection unchecked
        return (EnhancedIterator<TValue>) EMPTY;
    }
}