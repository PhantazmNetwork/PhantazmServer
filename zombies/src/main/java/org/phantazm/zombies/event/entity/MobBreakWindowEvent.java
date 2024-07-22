package org.phantazm.zombies.event.entity;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.trait.MobInstanceEvent;
import org.phantazm.zombies.event.trait.WindowEvent;
import org.phantazm.zombies.map.Window;

import java.util.Objects;

public class MobBreakWindowEvent implements MobInstanceEvent, WindowEvent {
    private final Mob self;
    private final Window window;
    private final int amount;

    public MobBreakWindowEvent(@NotNull Mob self, @NotNull Window window, int amount) {
        this.self = Objects.requireNonNull(self);
        this.window = Objects.requireNonNull(window);
        this.amount = amount;
    }

    @Override
    public @NotNull Mob mob() {
        return self;
    }

    public int amount() {
        return amount;
    }

    @Override
    public @NotNull Entity getEntity() {
        return self;
    }

    @Override
    public @NotNull Window window() {
        return window;
    }
}
