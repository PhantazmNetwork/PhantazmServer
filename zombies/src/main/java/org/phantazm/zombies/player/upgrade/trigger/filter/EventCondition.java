package org.phantazm.zombies.player.upgrade.trigger.filter;

import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

public interface EventCondition<T extends Event> {
    @NotNull Class<T> eventType();

    boolean filter(@NotNull T event);
}
