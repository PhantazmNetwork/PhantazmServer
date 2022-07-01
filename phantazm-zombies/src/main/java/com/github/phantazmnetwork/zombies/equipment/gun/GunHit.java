package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface GunHit {

    @NotNull Point getEnd();

    @NotNull Collection<PhantazmMob> getRegularTargets();

    @NotNull Collection<PhantazmMob> getHeadshotTargets();

}
