package org.phantazm.zombies.player.upgrade;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class GuardedPlayerUpgrade implements PlayerUpgrade {
    private final AtomicBoolean activated;
    private final Activable activable;
    private final ZombiesPlayer zombiesPlayer;

    public GuardedPlayerUpgrade(@NotNull Activable activable, @NotNull ZombiesPlayer zombiesPlayer) {
        this.activated = new AtomicBoolean();
        this.activable = Objects.requireNonNull(activable);
        this.zombiesPlayer = Objects.requireNonNull(zombiesPlayer);
    }

    @Override
    public final void start() {
        if (activated.compareAndSet(false, true)) {
            zombiesPlayer.addActivable(activable);
        }
    }

    @Override
    public final void tick(long time) {
        if (needsTicking() && activated.get()) {
            activable.tick(time);
        }
    }

    @Override
    public final void end() {
        if (activated.compareAndSet(true, false)) {
            zombiesPlayer.removeActivable(activable);
        }
    }

    @Override
    public final boolean isActivated() {
        return activated.get();
    }
}
