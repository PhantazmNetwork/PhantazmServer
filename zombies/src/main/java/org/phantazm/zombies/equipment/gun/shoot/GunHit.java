package org.phantazm.zombies.equipment.gun.shoot;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class GunHit {
    private final LivingEntity entity;
    private final Vec location;

    private boolean isHeadshot;

    public GunHit(@NotNull LivingEntity entity, @NotNull Vec location) {
        this.entity = Objects.requireNonNull(entity);
        this.location = Objects.requireNonNull(location);
    }

    public @NotNull LivingEntity entity() {
        return entity;
    }

    public @NotNull Vec location() {
        return location;
    }

    public boolean isHeadshot() {
        return isHeadshot;
    }

    public void setHeadshot() {
        isHeadshot = true;
    }
}
