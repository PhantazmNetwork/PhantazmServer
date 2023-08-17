package org.phantazm.mob2.skill;

public abstract class TimerSkillAbstract implements Skill {
    private final Skill delegate;
    private final boolean requiresActivation;
    private final boolean resetOnActivation;
    private final int repeat;
    private int interval;

    private final boolean tickDelegate;
    private boolean started;
    private int ticksSinceLastActivation;
    private int uses;

    private final Object lock = new Object();

    public TimerSkillAbstract(Skill delegate, boolean requiresActivation, boolean resetOnActivation, int repeat,
            int interval) {
        this.delegate = delegate;
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

        synchronized (lock) {
            if (started && resetOnActivation) {
                //case 1: timer is running, we reset on activation
                reset(true);
                return;
            }

            if (!started) {
                //case 2: timer is not running, start it
                started = true;
            }
        }
    }

    @Override
    public void tick() {
        if (tickDelegate) {
            delegate.tick();
        }

        boolean useDelegate = false;
        synchronized (lock) {
            if (!started) {
                return;
            }

            if (ticksSinceLastActivation == interval) {
                useDelegate = true;

                if (repeat > -1 && ++uses >= repeat) {
                    reset(false);
                }
                else {
                    ticksSinceLastActivation = 0;
                }

                interval = computeInterval();
            }
            else {
                ticksSinceLastActivation++;
            }
        }

        if (useDelegate) {
            delegate.use();
        }
    }

    @Override
    public void end() {
        synchronized (lock) {
            if (!started) {
                return;
            }

            reset(false);
            delegate.end();
        }
    }

    @Override
    public boolean needsTicking() {
        return true;
    }

    public abstract int computeInterval();
}
