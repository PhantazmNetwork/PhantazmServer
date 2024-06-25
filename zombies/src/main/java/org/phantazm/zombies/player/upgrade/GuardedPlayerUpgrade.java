package org.phantazm.zombies.player.upgrade;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GuardedPlayerUpgrade implements PlayerUpgrade {
    private final AtomicBoolean activated;

    public GuardedPlayerUpgrade() {
        this.activated = new AtomicBoolean();
    }

    @Override
    public final void start() {
        if (!activated.compareAndExchange(false, true)) {
            startGuarded();
        }
    }

    @Override
    public final void tick(long time) {
        if (activated.get()) {
            tickGuarded(time);
        }
    }

    @Override
    public final void end() {
        if (activated.compareAndExchange(true, false)) {
            endGuarded();
        }
    }

    @Override
    public final boolean isActivated() {
        return activated.get();
    }

    protected void startGuarded() {

    }

    protected void tickGuarded(long time) {

    }

    protected void endGuarded() {

    }
}
