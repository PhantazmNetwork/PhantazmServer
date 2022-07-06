package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import net.kyori.adventure.key.Keyed;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public interface TargetFinder {

    record Result(@NotNull Collection<GunHit> regular, @NotNull Collection<GunHit> headshot) {

        public Result {
            Objects.requireNonNull(regular, "regular");
            Objects.requireNonNull(headshot, "headshot");
        }

    }

    @NotNull Result findTarget(@NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
                               @NotNull Collection<UUID> previousHits);

    @NotNull Keyed getData();

}
