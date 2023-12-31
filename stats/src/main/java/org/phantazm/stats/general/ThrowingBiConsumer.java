package org.phantazm.stats.general;

public interface ThrowingBiConsumer<A, B, E extends Exception> {
    void accept(A a, B b) throws E;
}