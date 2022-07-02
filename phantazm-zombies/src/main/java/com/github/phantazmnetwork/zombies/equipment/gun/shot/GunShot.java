package com.github.phantazmnetwork.zombies.equipment.gun.shot;

import com.github.phantazmnetwork.mob.PhantazmMob;
import it.unimi.dsi.fastutil.Pair;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface GunShot {

    @NotNull Point getEnd();

    @NotNull Collection<Pair<PhantazmMob, Vec>> getRegularTargets();

    @NotNull Collection<Pair<PhantazmMob, Vec>> getHeadshotTargets();

}
