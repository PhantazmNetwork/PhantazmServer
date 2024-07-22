package org.phantazm.zombies.event.trait;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface MobInstanceEvent extends InstanceEvent, MobEvent {
    @Override
    default @NotNull Instance getInstance() {
        final Instance instance = getEntity().getInstance();
        assert instance != null : "MobInstanceEvent is only supported on events where the entity's instance is non-null!";
        return instance;
    }
}
