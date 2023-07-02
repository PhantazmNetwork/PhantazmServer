package org.phantazm.zombies.event;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.mob.PhantazmMobInstanceEvent;

public class PhantazmMobDeathEvent implements PhantazmMobInstanceEvent {
    private final PhantazmMob phantazmMob;

    public PhantazmMobDeathEvent(@NotNull PhantazmMob phantazmMob) {
        this.phantazmMob = phantazmMob;
    }

    @Override
    public @NotNull PhantazmMob getPhantazmMob() {
        return phantazmMob;
    }
}
