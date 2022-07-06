package com.github.phantazmnetwork.zombies.equipment.gun.shoot.handler;

import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunTickEffect;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunShot;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public interface ShotHandler extends GunTickEffect {

    void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
                @NotNull GunShot shot);

}
