package org.phantazm.core;

import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;

public enum TrackerTargetType {
    ENTITIES(EntityTracker.Target.ENTITIES),
    LIVING_ENTITIES(EntityTracker.Target.LIVING_ENTITIES),
    PLAYERS(EntityTracker.Target.PLAYERS),
    ITEMS(EntityTracker.Target.ITEMS),
    EXPERIENCE_ORBS(EntityTracker.Target.EXPERIENCE_ORBS);

    private final EntityTracker.Target<?> target;

    TrackerTargetType(EntityTracker.Target<?> target) {
        this.target = target;
    }

    public @NotNull EntityTracker.Target<?> target() {
        return target;
    }
}
