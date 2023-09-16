package org.phantazm.core.tick;

import net.minestom.server.Tickable;
import org.jetbrains.annotations.NotNull;

public interface TickTaskScheduler extends Tickable {
    void scheduleTaskNow(@NotNull TickableTask tickableTask);

    void scheduleTaskAfter(@NotNull TickableTask tickableTask, long ticks);

    void end();
}
