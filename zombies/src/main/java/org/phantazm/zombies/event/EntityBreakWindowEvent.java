package org.phantazm.zombies.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.event.trait.EntityInstanceEvent;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.map.Window;

import java.util.Objects;

public class EntityBreakWindowEvent implements EntityInstanceEvent {
    private final Entity entity;
    private final Window window;
    private final int amount;

    public EntityBreakWindowEvent(@NotNull Entity entity, @NotNull Window window, int amount) {
        this.entity = Objects.requireNonNull(entity, "entity");
        this.window = Objects.requireNonNull(window, "window");
        this.amount = amount;
    }

    @Override
    public @NotNull Entity getEntity() {
        return entity;
    }

    public @NotNull Window getWindow() {
        return window;
    }

    public int getAmount() {
        return amount;
    }
}
