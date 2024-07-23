package org.phantazm.zombies.event.entity;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.trait.LivingTargetEvent;
import org.phantazm.zombies.event.trait.MobTargetEvent;
import org.phantazm.zombies.event.trait.WindowEvent;
import org.phantazm.zombies.map.Window;

import java.util.Objects;

public class MobBreakWindowEvent implements MobTargetEvent, WindowEvent, LivingTargetEvent {
    private final Mob self;
    private final Window window;
    private final int amount;

    public MobBreakWindowEvent(@NotNull Mob self, @NotNull Window window, int amount) {
        this.self = Objects.requireNonNull(self);
        this.window = Objects.requireNonNull(window);
        this.amount = amount;
    }

    @Override
    public @NotNull Mob target() {
        return self;
    }

    @Override
    public @NotNull Window window() {
        return window;
    }

    public int amount() {
        return amount;
    }
}
