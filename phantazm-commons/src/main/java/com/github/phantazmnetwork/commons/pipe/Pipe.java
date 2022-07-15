package com.github.phantazmnetwork.commons.pipe;

import com.github.phantazmnetwork.commons.Wrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * <p>This interface represents an expansion upon the API offered by {@link Iterator}. Static methods are provided to
 * convert various objects into pipes. Default methods can be used to construct a pipe with specific properties, as
 * demonstrated below:</p>
 *
 * <pre>{@code
 * List<String> list = List.of("first", "second", "third", "fourth");
 * Pipe.Source<String> source = () -> Pipe.from(list.iterator()).filter(string -> string.startsWith("f"));
 *
 * for(String value : source) {
 *     System.out.println(value);
 * }
 * }</pre>
 *
 * <p>First, a list is constructed with arbitrary data. Then, a Pipe instance is created from the list's iterator.
 * {@code filter} is called on this instance, which returns a new Pipe that is filtered so that only those strings that
 * start with "f" may be iterated. Put a different way, the pipe (when iterated) now "looks like" a collection that only
 * contains the elements "first" and "fourth".</p>
 *
 * <p>Conceptually, pipes operate similarly to {@link Stream}s, the main difference being that pipes are themselves
 * instances of Iterator, and support several operations not present for streams (such as
 * {@link Pipe#listen(Consumer)}). The iteration order of a pipe depends on the data backing it: if the pipe is backed
 * by an iterator, the iteration order of the pipe is the same as the iterator. If it is backed by an array, the
 * iteration order is least to greatest by index.</p>
 *
 * @param <TValue> the type of element this pipe supplies
 * @implSpec Pipe implementations must guarantee that once {@code hasNext} returns false, it will continue to return
 * false for any subsequent invocations. Likewise, a call to {@code next} must throw a {@link NoSuchElementException} if
 * {@code hasNext} returns false. Failure to do so may result in undefined behavior from the pipes returned by the
 * methods in this class.
 */
public interface Pipe<TValue> extends Iterator<TValue> {
    /**
     * The shared "empty" pipe whose {@link Iterator#hasNext()} method always returns false. {@link Iterator#next()}
     * will throw a {@link NoSuchElementException}. This instance can safely be cast to any generic Pipe; however, if an
     * empty Pipe of a specific kind is needed, it is preferred to use {@link Pipe#empty()}.
     */
    Pipe<?> EMPTY = new Pipe<>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new NoSuchElementException();
        }
    };

    /**
     * <p>Creates a new pipe which produces elements until the given predicate no longer returns {@code true}. Calling
     * {@link Wrapper#set(Object)} (or another mutating method) on the predicate's argument will set the value to be
     * returned for this iteration. If no mutating method is called, the same value will be returned.</p>
     *
     * <p>This method is primarily useful for creating pipes based on a condition rather than a pre-existing data set.
     * For example, the following code uses pipes to find the largest hailstone sequence in the range [1, 1000]:</p>
     *
     * <pre>{@code
     *         int max = Pipe.whileTrue(wrapper -> wrapper.apply(value -> value + 1) <= 1000, 0).map(value -> {
     *             int count;
     *             for(count = 1; value != 1; count++) {
     *                 value = (value & 1) == 0 ? value >> 1 : value * 3 + 1;
     *             }
     *             return count;
     *         }).max(Integer::compareTo).orElseThrow();
     * }
     * </pre>
     *
     * @param predicate    the predicate which will determine pipe termination when it returns false
     * @param initialValue the initial value to assign to the wrapper
     * @param <TValue>     the type of value returned by the new pipe
     * @return a new pipe which will iterate until the given predicate returns false
     */
    static <TValue> @NotNull Pipe<TValue> whileTrue(@NotNull Predicate<Wrapper<TValue>> predicate,
                                                    @Nullable TValue initialValue) {
        return new Advancing<>() {
            private final Wrapper<TValue> value = Wrapper.of(initialValue);

            @Override
            protected boolean advance() {
                boolean result = predicate.test(value);
                if (!result) {
                    return false;
                }

                super.value = value.get();
                return true;
            }
        };
    }

    /**
     * Equivalent to {@link Pipe#whileTrue(Predicate, Object)}, with an initial value of {@code null}.
     *
     * @param predicate the predicate which will determine pipe termination when it returns false
     * @param <TValue>  the type of value returned by the new pipe
     * @return a new pipe which will iterate until the given predicate returns false, with an initial value of null
     * @see Pipe#whileTrue(Predicate, Object)
     */
    static <TValue> @NotNull Pipe<TValue> whileTrue(@NotNull Predicate<Wrapper<TValue>> predicate) {
        return whileTrue(predicate, null);
    }

    /**
     * Produces a new Pipe whose elements are the same as the provided iterator.
     *
     * @param iterator the iterator from which to create a pipe from
     * @param <TValue> the type of element supplied by the iterator
     * @return a new Pipe implementation from the given iterator. If {@code iterator} is already a pipe,
     * {@code iterator} is returned
     */
    static <TValue> @NotNull Pipe<TValue> from(@NotNull Iterator<? extends TValue> iterator) {
        Objects.requireNonNull(iterator, "iterator");

        if (iterator instanceof Pipe<?> pipe) {
            //noinspection unchecked
            return (Pipe<TValue>)pipe;
        }

        return new IteratorPipe<>(iterator);
    }

    /**
     * Produces a new Pipe from the given {@link Iterable}'s iterator.
     *
     * @param iterable the iterable providing an iterator from which to create a pipe from
     * @param <TValue> the type of element supplied by the iterator
     * @return a new Pipe implementation from the given iterable. If {@code iterable} is an instance of Pipe.Source,
     * its pipe is returned directly
     */
    static <TValue> @NotNull Pipe<TValue> from(@NotNull Iterable<? extends TValue> iterable) {
        Objects.requireNonNull(iterable, "iterable");

        if (iterable instanceof Pipe.Source<?> source) {
            //noinspection unchecked
            return (Pipe<TValue>)source.iterator();
        }

        return new IteratorPipe<>(iterable.iterator());
    }

    /**
     * Produces a new pipe whose elements are the same as the given array.
     *
     * @param elements the elements from which to create this pipe
     * @param <TValue> the type of element contained in the array
     * @return a new pipe whose elements are the same as the given array
     * @throws NullPointerException if elements is null
     */
    @SafeVarargs
    static <TValue> @NotNull Pipe<TValue> of(TValue @NotNull ... elements) {
        Objects.requireNonNull(elements, "elements");

        if (elements.length == 0) {
            return empty();
        }
        else if (elements.length == 1) {
            return new SingletonPipe<>(elements[0]);
        }
        else {
            return new ArrayPipe<>(elements);
        }
    }

    /**
     * Returns the shared, empty Pipe ({@link Pipe#EMPTY}) after casting to the appropriate return value.
     *
     * @param <TValue> the element type this pipe should appear to contain
     * @return the shared empty pipe, after casting to the return value
     */
    static <TValue> @NotNull Pipe<TValue> empty() {
        //noinspection unchecked
        return (Pipe<TValue>)EMPTY;
    }

    /**
     * Creates a new Pipe bound to this one which will apply the provided mapping function to each value in turn during
     * iteration.
     *
     * @param mapper the mapping function used to convert each element
     * @param <TOut> the element type of the new pipe
     * @return a new Pipe containing the results of applying the given mapping function to this pipe
     * @throws NullPointerException if mapper is null
     */
    default <TOut> @NotNull Pipe<TOut> map(@NotNull Function<? super TValue, ? extends TOut> mapper) {
        Objects.requireNonNull(mapper, "mapper");

        return new Pipe<>() {
            @Override
            public boolean hasNext() {
                return Pipe.this.hasNext();
            }

            @Override
            public TOut next() {
                return mapper.apply(Pipe.this.next());
            }
        };
    }

    /**
     * Creates a new Pipe by applying a mapping function to each element of this pipe. The mapping function itself
     * returns an {@link Iterator}, each element of which will be present in the new pipe and combined with any other
     * Iterators produced by the mapping function.
     *
     * @param mapper the mapping function which produces Iterators from the elements of this pipe
     * @param <TOut> the element type of the new pipe
     * @return a new Pipe containing the results of applying the given mapping function to this pipe, and combining
     * each element produced by the Iterators the mapping function returns
     * @throws NullPointerException if mapper is null
     */
    default <TOut> @NotNull Pipe<TOut> flatMap(
            @NotNull Function<? super TValue, ? extends Iterator<? extends TOut>> mapper) {
        Objects.requireNonNull(mapper, "mapper");

        return new Advancing<>() {
            private Iterator<? extends TOut> current;

            @Override
            protected boolean advance() {
                while (current == null || !current.hasNext()) {
                    if (!Pipe.this.hasNext()) {
                        return false;
                    }

                    current = mapper.apply(Pipe.this.next());
                }

                super.value = current.next();
                return true;
            }
        };
    }

    /**
     * Creates a new pipe whose elements will be restricted to only those which satisfy the given predicate.
     *
     * @param filter the predicate used to determine which elements may exist in the new pipe
     * @return a new pipe which will only contain elements that satisfy the given predicate
     * @throws NullPointerException if filter is null
     */
    default @NotNull Pipe<TValue> filter(@NotNull Predicate<? super TValue> filter) {
        Objects.requireNonNull(filter, "filter");

        return new Advancing<>() {
            @Override
            protected boolean advance() {
                while (Pipe.this.hasNext()) {
                    TValue value = Pipe.this.next();
                    if (filter.test(value)) {
                        super.value = value;
                        return true;
                    }
                }

                return false;
            }
        };
    }

    /**
     * Creates a new pipe with the same elements as this one, where iteration will progress until one of the elements
     * meets the given predicate. When this occurs, the new Pipe will stop producing values. The element which meets the
     * condition is <i>not</i> included in the new pipe's iteration.
     *
     * @param condition the termination condition
     * @return a new pipe whose iteration will terminate when the given condition is met
     * @throws NullPointerException if condition is null
     */
    default @NotNull Pipe<TValue> until(@NotNull Predicate<? super TValue> condition) {
        Objects.requireNonNull(condition, "condition");

        return new Advancing<>() {
            @Override
            protected boolean advance() {
                if (!Pipe.this.hasNext()) {
                    return false;
                }

                TValue value = Pipe.this.next();
                if (condition.test(value)) {
                    return false;
                }

                super.value = value;
                return true;
            }
        };
    }

    /**
     * Creates a new pipe whose elements are the same as this, but the provided {@link Consumer} will be called for each
     * element, only as it is iterated.
     *
     * @param listener the consumer to call for every element
     * @return a new pipe which will sequentially call the consumer with every element produced during iteration
     * @throws NullPointerException if listener is null
     */
    default @NotNull Pipe<TValue> listen(@NotNull Consumer<? super TValue> listener) {
        Objects.requireNonNull(listener, "listener");

        return new Pipe<>() {
            @Override
            public boolean hasNext() {
                return Pipe.this.hasNext();
            }

            @Override
            public TValue next() {
                TValue value = Pipe.this.next();
                listener.accept(value);
                return value;
            }
        };
    }

    /**
     * Creates a new pipe which will call the provided {@link Consumer} when the last element of this pipe is reached.
     * The "last" element is defined as the first element returned by a call to {@code next} for which a subsequent call
     * to {@code hasNext} returns false. If this pipe has no elements to begin with, the consumer will never be called.
     *
     * @param consumer the consumer which receives the final element of the pipe
     * @return a new pipe which will call the given Consumer with the final element when it is reached
     * @throws NullPointerException if consumer is null
     */
    default @NotNull Pipe<TValue> forLast(@NotNull Consumer<? super TValue> consumer) {
        Objects.requireNonNull(consumer, "action");

        return new Pipe<>() {
            @Override
            public boolean hasNext() {
                return Pipe.this.hasNext();
            }

            @Override
            public TValue next() {
                TValue next = Pipe.this.next();
                if (!Pipe.this.hasNext()) {
                    consumer.accept(next);
                }

                return next;
            }
        };
    }

    /**
     * Drains (empties, or iterates) this pipe, calling the provided {@link Consumer} with every element. This is
     * equivalent to {@link Iterator#forEachRemaining(Consumer)}.
     *
     * @param consumer the consumer to sequentially receive each element
     * @throws NullPointerException if consumer is null
     */
    default void drain(@NotNull Consumer<? super TValue> consumer) {
        Objects.requireNonNull(consumer, "consumer");

        while (hasNext()) {
            consumer.accept(next());
        }
    }

    /**
     * Drains the pipe, discarding all values.
     */
    default void drain() {
        while (hasNext()) {
            next();
        }
    }

    /**
     * Drains this pipe, performing a reduction operation using the provided {@link BinaryOperator}. Can be used to
     * "merge" all elements of a pipe into a single value. For example:
     *
     * <pre>{@code
     * String merged = Pipe.of("a", "b", "c").reduce(String::concat).orElseThrow();
     * }
     * </pre>
     *
     * <p>Here, {@code merged} will be equal to "abc".</p>
     *
     * @param operator the BinaryOperator used to merge elements of this pipe
     * @return an {@link Optional} containing the result of the reduction, which will be empty if this pipe is empty
     */
    default @NotNull Optional<TValue> reduce(@NotNull BinaryOperator<TValue> operator) {
        Objects.requireNonNull(operator, "operator");

        boolean foundAny = false;
        TValue running = null;
        while (hasNext()) {
            if (!foundAny) {
                foundAny = true;
                running = next();
            }
            else {
                running = operator.apply(running, next());
            }
        }

        return foundAny ? Optional.of(running) : Optional.empty();
    }

    /**
     * Works similarly to {@link Pipe#reduce(BinaryOperator)}, but accepts an initial value which will be passed to the
     * {@link BinaryOperator}, along with the first element of this pipe.
     *
     * @param initial  the initial value
     * @param operator the BinaryOperator used to merge elements of this pipe
     * @return the result of the reduction operation
     */
    default TValue reduce(TValue initial, @NotNull BinaryOperator<TValue> operator) {
        Objects.requireNonNull(operator, "operator");

        TValue running = initial;
        while (hasNext()) {
            running = operator.apply(running, next());
        }

        return running;
    }

    /**
     * Drains this pipe, returning the smallest element as determined by the given {@link Comparator}.
     *
     * @param comparator the comparator to use for determining the smallest value
     * @return an {@link Optional} containing the smallest value returned by the pipe, or empty if the pipe is empty
     */
    default Optional<TValue> min(@NotNull Comparator<? super TValue> comparator) {
        return reduce(BinaryOperator.minBy(comparator));
    }

    /**
     * Drains this pipe, returning the largest element as determined by the given {@link Comparator}.
     *
     * @param comparator the comparator to use for determining the largest value
     * @return an {@link Optional} containing the largest value returned by the pipe, or empty if the pipe is empty
     */
    default Optional<TValue> max(@NotNull Comparator<? super TValue> comparator) {
        return reduce(BinaryOperator.maxBy(comparator));
    }

    /**
     * Drains this pipe, checking each element against the provided Predicate. If the predicate returns true for any
     * element, this method returns true. Otherwise, returns false. If this method returns true, any remaining elements
     * in this pipe may not have been iterated.
     *
     * @param predicate the predicate to use
     * @return true if at least one element was found that matches the given predicate, false otherwise
     */
    default boolean any(@NotNull Predicate<? super TValue> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        while (hasNext()) {
            if (predicate.test(next())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Drains this pipe, checking each element against the provided Predicate. If the predicate returns false for all
     * elements, this method returns true. Otherwise, returns false. If this method returns false, any remaining
     * elements in this pipe may not have been iterated.
     *
     * @param predicate the predicate to use
     * @return true if no elements match the given predicate, false otherwise
     */
    default boolean none(@NotNull Predicate<? super TValue> predicate) {
        return !any(predicate);
    }

    /**
     * Represents a supplier of Pipe objects. This interface extends {@link Iterable}, and thus may be used as the
     * target of enhanced-{@code for} loops.
     *
     * @param <TValue> the type of element supplied by the returned Pipe
     */
    @FunctionalInterface
    interface Source<TValue> extends Iterable<TValue> {
        @Override
        @NotNull Pipe<TValue> iterator();
    }

    /**
     * <p>An abstract subclass of Pipe that allows for an alternate mechanism of iteration — advancing. Instead of
     * separating the logic of determining if an element exists ({@code hasNext}) and the logic for computing/returning
     * said element ({@code next}), this class combines both steps into a single method {@link Advancing#advance()}.
     * This is primarily useful when creating a pipe that computes values on the fly.</p>
     *
     * <p>On a surface level, this class appears to behave identically to typical Iterators during iteration. However,
     * to enable the necessary logic behind the operation of this class, {@link Advancing#hasNext()} is not stateless.
     * </p>
     *
     * <p>This class enforces the pipe invariant that {@code hasNext} should never return true again after it has
     * returned false once. Therefore, after {@code advance} returns false once, it will never be called again during
     * iteration, and the pipe will be drained.</p>
     *
     * @param <TValue> the type of element this pipe supplies
     */
    abstract class Advancing<TValue> implements Pipe<TValue> {
        /**
         * The value currently held by this pipe.
         */
        protected TValue value;
        private boolean ended;
        private boolean hasValue;

        @Override
        public final boolean hasNext() {
            if (ended) {
                return false;
            }

            //handle redundant calls to hasNext if the next value exists and has not been consumed by returning true
            if (hasValue) {
                return true;
            }

            hasValue = advance();
            if (!hasValue) {
                ended = true;
            }

            return hasValue;
        }

        @Override
        public final TValue next() {
            if (ended) {
                throw new NoSuchElementException();
            }

            //we don't currently have a value...
            if (!hasValue) {
                //...so try to advance; if we fail to advance here then throw an exception
                //this corresponds to "unsafe" calls to next() that aren't preceded by hasNext()
                if (!advance()) {
                    ended = true;
                    value = null;
                    throw new NoSuchElementException();
                }
            }

            hasValue = false;

            TValue valueCopy = value;
            value = null;

            return valueCopy;
        }

        /**
         * <p>Attempts to compute the next value. If this computation is successful, the result must be assigned to the
         * field {@link Advancing#value}, and this method must return {@code true}. If this method returns
         * {@code false}, the Pipe is considered to have been emptied, which will cause iteration to terminate.</p>
         *
         * <p>This method should never throw a runtime exception. If this Pipe is being used as an iterator, doing so
         * may violate the expectation that {@link Iterator#hasNext()} will not throw an exception.</p>
         *
         * @return {@code true} if the computation succeeded, {@code false} otherwise
         */
        protected abstract boolean advance();
    }
}
