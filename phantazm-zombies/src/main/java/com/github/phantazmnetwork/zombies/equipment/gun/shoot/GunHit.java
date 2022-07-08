package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents an individual hit target of a gun. A gun may have multiple hits in a single fire.
 * @param entity The target {@link LivingEntity} of the hit
 * @param location The location of the hit
 */
public record GunHit(@NotNull LivingEntity entity, @NotNull Vec location) {

    /**
     * Creates a {@link GunHit}.
     * @param entity The target {@link LivingEntity} of the hit
     * @param location The location of the hit
     */
    public GunHit {
        Objects.requireNonNull(entity, "mob");
        Objects.requireNonNull(location, "location");
    }

}
