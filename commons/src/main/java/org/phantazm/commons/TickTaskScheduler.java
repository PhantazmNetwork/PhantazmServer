package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

public interface TickTaskScheduler extends Tickable {
    void scheduleTaskNow(@NotNull TickableTask tickableTask);

    void scheduleTaskAfter(@NotNull TickableTask tickableTask, long millis);

    void end();
}
