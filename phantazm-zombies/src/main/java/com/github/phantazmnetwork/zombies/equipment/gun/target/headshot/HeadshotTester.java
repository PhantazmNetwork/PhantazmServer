package com.github.phantazmnetwork.zombies.equipment.gun.target.headshot;

import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

public interface HeadshotTester {

    boolean isHeadshot(@NotNull Entity shooter, @NotNull Entity entity, @NotNull Point intersection);

    @NotNull Keyed getData();

}
