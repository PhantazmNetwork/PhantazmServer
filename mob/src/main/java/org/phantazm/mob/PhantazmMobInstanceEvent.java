package org.phantazm.mob;

import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

public interface PhantazmMobInstanceEvent extends PhantazmMobEvent, InstanceEvent {
    @Override
    default @NotNull Instance getInstance() {
        Instance instance = getPhantazmMob().entity().getInstance();
        assert instance !=
                null : "PhantazmMobInstanceEvent is only supported on events where the entity's instance is non-null!";
        return instance;
    }
}
