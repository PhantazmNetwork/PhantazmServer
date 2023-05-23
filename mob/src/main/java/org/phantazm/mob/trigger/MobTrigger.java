package org.phantazm.mob.trigger;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;

public interface MobTrigger extends Keyed {
    void initialize(@NotNull EventNode<Event> node, @NotNull MobStore store);
}
