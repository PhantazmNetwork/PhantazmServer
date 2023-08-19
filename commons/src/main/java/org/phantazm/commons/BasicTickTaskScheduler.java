package org.phantazm.commons;

import org.jetbrains.annotations.NotNull;

import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BasicTickTaskScheduler implements TickTaskScheduler {
    private final Deque<TickableTask> tickableTasks;

    public BasicTickTaskScheduler() {
        this.tickableTasks = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void scheduleTaskNow(@NotNull TickableTask tickableTask) {
        Objects.requireNonNull(tickableTask);
        tickableTasks.add(tickableTask);
    }

    @Override
    public void scheduleTaskAfter(@NotNull TickableTask tickableTask, long ticks) {
        Objects.requireNonNull(tickableTask);
        tickableTasks.add(new TickableTask() {

            private long duration = 0;

            private boolean finished;

            @Override
            public boolean isFinished() {
                return finished;
            }

            @Override
            public void tick(long time) {
                ++duration;
                if (duration >= ticks) {
                    tickableTasks.add(tickableTask);
                    finished = true;
                }
            }
        });
    }

    @Override
    public void end() {
        for (TickableTask tickableTask : tickableTasks) {
            tickableTask.end();
        }

        tickableTasks.clear();
    }

    @Override
    public void tick(long time) {
        Iterator<TickableTask> taskIterator = tickableTasks.iterator();
        while (taskIterator.hasNext()) {
            TickableTask next = taskIterator.next();
            if (next.isFinished()) {
                taskIterator.remove();
                continue;
            }

            next.tick(time);
        }
    }
}
