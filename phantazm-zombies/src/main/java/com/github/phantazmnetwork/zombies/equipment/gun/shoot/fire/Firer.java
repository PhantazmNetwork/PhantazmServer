package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire;

import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.GunState;
import com.github.phantazmnetwork.zombies.equipment.gun.effect.GunTickEffect;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface Firer extends GunTickEffect {

    void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<PhantazmMob> previousHits);

}
