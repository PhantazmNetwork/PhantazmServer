package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.mob.PhantazmMob;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface HeadshotTester {

    boolean isHeadshot(@NotNull Player player, @NotNull PhantazmMob mob, @NotNull Point intersection);

    @NotNull Keyed getData();

}
