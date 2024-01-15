package org.phantazm.mob2.skill;

import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

import java.util.Objects;

public abstract class TimerSkillAbstract implements Skill {
    private final Scheduler scheduler;
    private final Mob self;
    private final Skill delegate;
    private final boolean requiresActivation;
    private final boolean resetOnActivation;
    private final int repeat;
    private final boolean tickDelegate;
    private final boolean endImmediately;

    private int interval;
    private boolean started;
    private int ticksSinceLastActivation;
    private int uses;

    private Task scheduledTask;

    public TimerSkillAbstract(@NotNull Scheduler scheduler, @NotNull Mob self, @NotNull Skill delegate,
        boolean requiresActivation, boolean resetOnActivation, int repeat, int interval, boolean endImmediately) {
        this.scheduler = Objects.requireNonNull(scheduler);
        this.self = Objects.requireNonNull(self);
        this.delegate = Objects.requireNonNull(delegate);
        this.requiresActivation = requiresActivation;
        this.resetOnActivation = resetOnActivation;
        this.repeat = repeat;
        this.interval = interval;
        this.tickDelegate = delegate.needsTicking();
        this.endImmediately = endImmediately;

        this.started = !requiresActivation;
        this.ticksSinceLastActivation = 0;
        this.uses = 0;
    }

    private void reset(boolean started) {
        this.ticksSinceLastActivation = 0;
        this.uses = 0;
        this.started = started;
    }

    @Override
    public void init() {
        delegate.init();
    }

    @Override
    public void use() {
        if (!resetOnActivation && !requiresActivation) {
            //if we don't reset on activation and don't require activation, use() does nothing
            return;
        }

        self.getAcquirable().sync(ignored -> {
            if (started && resetOnActivation) {
                //case 1: timer is running, we reset on activation
                //since started is true, we don't have a running task
                reset(true);
                return;
            }

            if (!started) {
                //we're starting, so transition back to using the normal tick method
                cancelTask();

                //case 2: timer is not running, start it
                started = true;
            }
        });
    }

    @Override
    public void tick() {
        if (tickDelegate) {
            delegate.tick();
        }

        if (!started) {
            return;
        }

        tick0();
    }

    private boolean tick0() {
        if (ticksSinceLastActivation == interval) {
            if (repeat > -1 && ++uses > repeat) {
                reset(false);
                return true;
            } else {
                ticksSinceLastActivation = 0;
            }

            interval = computeInterval();
            delegate.use();
        } else {
            ticksSinceLastActivation++;
        }

        return false;
    }

    @Override
    public void end() {
        self.getAcquirable().sync(ignored -> {
            cancelTask();
            if (!started) {
                return;
            }

            //if endImmediately, the timer will stop as soon as end() is called
            if (endImmediately) {
                reset(false);
                delegate.end();
                return;
            }

            this.started = false;

            //pass over responsibility of ticking to the scheduler
            Wrapper<Task> taskWrapper = Wrapper.ofNull();
            Task task = scheduler.scheduleTask(() -> {
                if (tick0()) {
                    Task thisTask = taskWrapper.get();
                    thisTask.cancel();

                    self.getAcquirable().sync(self -> {
                        if (this.scheduledTask == thisTask) {
                            this.scheduledTask = null;
                        }
                    });
                }
            }, TaskSchedule.nextTick(), TaskSchedule.tick(1));
            taskWrapper.set(task);

            setScheduledTask(task);
        });
    }

    private void cancelTask() {
        Task task = this.scheduledTask;
        if (task != null) {
            task.cancel();
            this.scheduledTask = null;
        }
    }

    private void setScheduledTask(Task task) {
        Task oldTask = this.scheduledTask;
        if (oldTask != null) {
            oldTask.cancel();
        }

        this.scheduledTask = task;
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    public abstract int computeInterval();
}
