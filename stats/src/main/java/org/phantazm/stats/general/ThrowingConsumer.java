package org.phantazm.stats.general;

public interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E;
}
