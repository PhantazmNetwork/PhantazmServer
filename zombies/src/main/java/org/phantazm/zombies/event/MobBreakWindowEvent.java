package org.phantazm.zombies.event;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.MobEvent;
import org.phantazm.zombies.map.Window;

import java.util.Objects;

public class MobBreakWindowEvent implements MobEvent {
    private final Mob self;
    private final Window window;
    private final int amount;

    public MobBreakWindowEvent(@NotNull Mob self, @NotNull Window window, int amount) {
        this.self = Objects.requireNonNull(self);
        this.window = Objects.requireNonNull(window);
        this.amount = amount;
    }

    @Override
    public @NotNull Entity getEntity() {
        return self;
    }

    @Override
    public @NotNull Mob mob() {
        return self;
    }

    public @NotNull Window getWindow() {
        return window;
    }

    public int getAmount() {
        return amount;
    }
}
