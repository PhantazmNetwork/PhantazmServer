package org.phantazm.mob.trigger;

import net.kyori.adventure.key.Key;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Namespaces;
import org.phantazm.mob.MobStore;

public class EmptyTrigger implements MobTrigger {
    public static final EmptyTrigger INSTANCE = new EmptyTrigger();

    private static final Key KEY = Key.key(Namespaces.PHANTAZM, "empty");

    private EmptyTrigger() {

    }

    @Override
    public void initialize(@NotNull EventNode<Event> node, @NotNull MobStore store) {
        //no-op, this trigger does nothing
    }

    @Override
    public @NotNull Key key() {
        return KEY;
    }
}
