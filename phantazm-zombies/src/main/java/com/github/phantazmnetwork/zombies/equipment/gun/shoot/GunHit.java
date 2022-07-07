package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record GunHit(@NotNull LivingEntity entity, @NotNull Vec location) {

    public GunHit {
        Objects.requireNonNull(entity, "mob");
        Objects.requireNonNull(location, "location");
    }

}
