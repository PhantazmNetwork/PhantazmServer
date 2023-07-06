package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

public interface TickableTask extends Tickable {
    boolean isFinished();

    default void end() {

    }

    static @NotNull TickableTask afterTicks(long ticks, @NotNull Runnable action, @NotNull Runnable endAction) {
        long start = System.currentTimeMillis();
        return new TickableTask() {
            private boolean finished;

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public void tick(long time) {
                if ((time - start) / 50 >= ticks) {
                    finished = true;
                    action.run();
                }
            }

            @Override
            public void end() {
                if (!finished) {
                    endAction.run();
                }
            }
        };
    }
}
