package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

import java.util.Objects;

public abstract class TimerSkillAbstract implements Skill {
    private final Mob self;
    private final Skill delegate;
    private final boolean requiresActivation;
    private final boolean resetOnActivation;
    private final int repeat;
    private final boolean tickDelegate;
    private int interval;
    private boolean started;
    private int ticksSinceLastActivation;
    private int uses;

    public TimerSkillAbstract(@NotNull Mob self, @NotNull Skill delegate, boolean requiresActivation,
        boolean resetOnActivation, int repeat, int interval) {
        this.self = Objects.requireNonNull(self);
        this.delegate = Objects.requireNonNull(delegate);
        this.requiresActivation = requiresActivation;
        this.resetOnActivation = resetOnActivation;
        this.repeat = repeat;
        this.interval = interval;

        this.tickDelegate = delegate.needsTicking();
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
                reset(true);
                return;
            }

            if (!started) {
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

        if (ticksSinceLastActivation == interval) {
            if (repeat > -1 && ++uses >= repeat) {
                reset(false);
            } else {
                ticksSinceLastActivation = 0;
            }

            interval = computeInterval();
            delegate.use();
        } else {
            ticksSinceLastActivation++;
        }
    }

    @Override
    public void end() {
        self.getAcquirable().sync(ignored -> {
            if (!started) {
                return;
            }

            reset(false);
            delegate.end();
        });
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    public abstract int computeInterval();
}
