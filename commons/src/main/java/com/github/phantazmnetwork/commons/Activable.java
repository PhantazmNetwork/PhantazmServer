package com.github.phantazmnetwork.commons;

public interface Activable extends Tickable {

    default void start() {

    }

    @Override
    default void tick(long time) {

    }

    default void end() {

    }

}
