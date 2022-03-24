package com.github.phantazmnetwork.commons.vector;

@FunctionalInterface
public interface Vec3IFunction<TReturn> {
    TReturn apply(int x, int y, int z);
}
