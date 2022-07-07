package com.github.phantazmnetwork.zombies.equipment.gun.shoot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public record GunShot(@NotNull Pos start,
                      @NotNull Point end,
                      @NotNull Collection<GunHit> regularTargets,
                      @NotNull Collection<GunHit> headshotTargets) {

    public GunShot {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(regularTargets, "regularTargets");
        Objects.requireNonNull(headshotTargets, "headshotTargets");
    }


}
