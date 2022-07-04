package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record GunHit(@NotNull PhantazmMob mob, @NotNull Vec location) {

    public GunHit {
        Objects.requireNonNull(mob, "mob");
        Objects.requireNonNull(location, "location");
    }

}
