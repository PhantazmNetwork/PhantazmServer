package com.github.phantazmnetwork.zombies.equipment.gun.target;

import net.kyori.adventure.key.Keyed;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public interface TargetFinder {

    record Result(@NotNull Collection<GunHit> regular, @NotNull Collection<GunHit> headshot) {

        public Result {
            Objects.requireNonNull(regular, "regular");
            Objects.requireNonNull(headshot, "headshot");
        }

    }

    @NotNull Result findTarget(@NotNull Player player, @NotNull Pos start, @NotNull Point end,
                               @NotNull Collection<PhantazmMob> previousHits);

    @NotNull Keyed getData();

}
