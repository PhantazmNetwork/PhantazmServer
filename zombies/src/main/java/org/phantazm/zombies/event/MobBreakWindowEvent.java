package org.phantazm.zombies.event;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.PhantazmMobInstanceEvent;
import org.phantazm.zombies.map.Window;

import java.util.Objects;

public class MobBreakWindowEvent implements PhantazmMobInstanceEvent {
    private final PhantazmMob phantazmMob;
    private final Window window;
    private final int amount;

    public MobBreakWindowEvent(@NotNull PhantazmMob phantazmMob, @NotNull Window window, int amount) {
        this.phantazmMob = Objects.requireNonNull(phantazmMob);
        this.window = Objects.requireNonNull(window);
        this.amount = amount;
    }

    @Override
    public @NotNull PhantazmMob getPhantazmMob() {
        return phantazmMob;
    }

    public @NotNull Window getWindow() {
        return window;
    }

    public int getAmount() {
        return amount;
    }
}
