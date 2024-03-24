package org.phantazm.mob2.skill;

import com.github.steanky.toolkit.collection.Wrapper;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.BasicMobSpawner;
import org.phantazm.mob2.Mob;

import java.util.Objects;

public abstract class TimerSkillAbstract implements Skill {
    protected static class Extension {
        private int interval;
        private boolean started;
        private int ticksSinceLastActivation;
        private int uses;

        private Task scheduledTask;

        private Extension(int interval, boolean started, int ticksSinceLastActivation, int uses) {
            this.interval = interval;
            this.started = started;
            this.ticksSinceLastActivation = ticksSinceLastActivation;
            this.uses = uses;
        }
    }

    private final ExtensionHolder.Key<Extension> key;

    private final Skill delegate;

    private final int initialInterval;
    private final boolean initialStarted;
    private final int initialTicksSinceLastActivation;
    private final int initialUses;

    private final boolean requiresActivation;
    private final boolean resetOnActivation;
    private final int repeat;
    private final boolean tickDelegate;
    private final boolean endImmediately;

    protected TimerSkillAbstract(@NotNull ExtensionHolder.Key<Extension> key,
        @NotNull Skill delegate, boolean requiresActivation, boolean resetOnActivation, int repeat, int interval,
        boolean endImmediately) {
        this.key = key;
        this.delegate = Objects.requireNonNull(delegate);
        this.requiresActivation = requiresActivation;
        this.resetOnActivation = resetOnActivation;
        this.repeat = repeat;
        this.initialInterval = interval;
        this.tickDelegate = delegate.needsTicking();
        this.endImmediately = endImmediately;

        this.initialStarted = !requiresActivation;
        this.initialTicksSinceLastActivation = 0;
        this.initialUses = 0;
    }

    private void reset(Extension ext, boolean started) {
        ext.ticksSinceLastActivation = 0;
        ext.uses = 0;
        ext.started = started;
    }

    @Override
    public void init(@NotNull Mob mob) {
        try {
            mob.extensions().set(key, new Extension(initialInterval, initialStarted, initialTicksSinceLastActivation,
                initialUses));
        } catch (ClassCastException ignored) {
            System.out.println(mob.data().key());
        }

        delegate.init(mob);
    }

    @Override
    public void use(@NotNull Mob mob) {
        if (!resetOnActivation && !requiresActivation) {
            //if we don't reset on activation and don't require activation, use() does nothing
            return;
        }

        Extension ext = mob.extensions().get(key);
        mob.getAcquirable().sync(ignored -> {
            if (ext.started && resetOnActivation) {
                //case 1: timer is running, we reset on activation
                //since started is true, we don't have a running task
                reset(ext, true);
                return;
            }

            if (!ext.started) {
                //we're starting, so transition back to using the normal tick method
                cancelTask(ext);

                //case 2: timer is not running, start it
                ext.started = true;
            }
        });
    }

    @Override
    public void tick(@NotNull Mob mob) {
        if (tickDelegate) {
            delegate.tick(mob);
        }

        Extension ext = mob.extensions().get(key);
        if (!ext.started) {
            return;
        }

        tick0(ext, mob);
    }

    private boolean tick0(Extension ext, Mob mob) {
        if (ext.ticksSinceLastActivation == ext.interval) {
            if (repeat > -1 && ++ext.uses > repeat) {
                reset(ext, false);
                return true;
            } else {
                ext.ticksSinceLastActivation = 0;
            }

            ext.interval = computeInterval();
            delegate.use(mob);
        } else {
            ext.ticksSinceLastActivation++;
        }

        return false;
    }

    @Override
    public void end(@NotNull Mob mob) {
        Extension ext = mob.extensions().get(key);
        mob.getAcquirable().sync(ignored -> {
            cancelTask(ext);
            if (!ext.started) {
                return;
            }

            //if endImmediately, the timer will stop as soon as end() is called
            //if the timer is infinite, end immediately and don't schedule a task
            if (endImmediately || repeat < 0) {
                reset(ext, false);
                delegate.end(mob);
                return;
            }

            ext.started = false;

            //pass over responsibility of ticking to the scheduler
            Wrapper<Task> taskWrapper = Wrapper.ofNull();
            Scheduler scheduler = mob.extensions().getOrDefault(BasicMobSpawner.SCHEDULER_KEY,
                MinecraftServer::getSchedulerManager);
            Task task = scheduler.scheduleTask(() -> {
                if (tickDelegate) {
                    delegate.tick(mob);
                }

                if (tick0(ext, mob)) {
                    Task thisTask = taskWrapper.get();
                    thisTask.cancel();

                    mob.getAcquirable().sync(self -> {
                        if (ext.scheduledTask == thisTask) {
                            ext.scheduledTask = null;
                        }
                    });
                }
            }, TaskSchedule.nextTick(), TaskSchedule.nextTick());
            taskWrapper.set(task);

            setScheduledTask(ext, task);
        });
    }

    private void cancelTask(Extension ext) {
        Task task = ext.scheduledTask;
        if (task != null) {
            task.cancel();
            ext.scheduledTask = null;
        }
    }

    private void setScheduledTask(Extension ext, Task task) {
        Task oldTask = ext.scheduledTask;
        if (oldTask != null) {
            oldTask.cancel();
        }

        ext.scheduledTask = task;
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    public abstract int computeInterval();
}
