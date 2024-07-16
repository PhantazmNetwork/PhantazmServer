package org.phantazm.zombies.equipment.gun.target;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;

import java.util.Collection;
import java.util.UUID;

public interface TargetFinder {

    @NotNull Result findTarget(@NotNull Gun gun, @NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
        @NotNull Collection<UUID> previousHits);

    record Result(@NotNull Collection<GunHit> hits) {


    }

}
