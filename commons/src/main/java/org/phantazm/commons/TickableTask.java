package org.phantazm.commons;

public interface TickableTask extends Tickable {
    boolean isFinished();

    default void end() {

    }
}
