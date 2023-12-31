package org.phantazm.stats.general;

public interface ThrowingBiFunction<A, B, R, E extends Exception> {
    R apply(A a, B b) throws E;
}
