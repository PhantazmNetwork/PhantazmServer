package com.github.phantazmnetwork.commons;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * Contains static utility methods related to multithreaded synchronization, particularly locks. This class cannot be
 * instantiated.
 * @see Lock
 */
public final class LockUtils {
    private LockUtils() { throw new UnsupportedOperationException(); }

    /**
     * Executes the provided {@link Supplier}, returning its result, after acquiring the given lock. The lock will
     * always be released after the supplier executes, even if it throws an unchecked exception.
     * @param lock the lock to acquire before calling the supplier
     * @param supplier the supplier used to produce the returned value
     * @param <TReturn> the type of value returned by the supplier
     * @return the value returned by the supplier
     */
    public static <TReturn> TReturn lock(@NotNull Lock lock, @NotNull Supplier<? extends TReturn> supplier) {
        try {
            lock.lock();
            return supplier.get();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Executes the provided {@link Runnable} after acquiring the given lock. The lock will always be released after the
     * runnable executes, even if it throws an unchecked exception.
     * @param lock the lock to acquire before calling the runnable
     * @param runnable the runnable to execute
     */
    public static void lock(@NotNull Lock lock, @NotNull Runnable runnable) {
        try {
            lock.lock();
            runnable.run();
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * Executes the provided {@link Callable}, returning its result, after acquiring the given lock. The lock will
     * always be released after the callable executes, even if it throws an exception.
     * @apiNote The name of this method differs from {@link LockUtils#lock(Lock, Supplier)} to avoid ambiguity issues.
     * @param lock the lock to acquire before calling the callable
     * @param callable the callable used to produce the returned value
     * @param <TReturn>> the type of value returned by the callable
     * @return the value returned by the callable
     */
    public static <TReturn> TReturn lockCallable(@NotNull Lock lock, @NotNull Callable<? extends TReturn> callable)
            throws Exception {
        try {
            lock.lock();
            return callable.call();
        }
        finally {
            lock.unlock();
        }
    }
}
