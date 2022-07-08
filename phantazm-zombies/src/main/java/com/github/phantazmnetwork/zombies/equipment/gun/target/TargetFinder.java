package com.github.phantazmnetwork.zombies.equipment.gun.target;

import com.github.phantazmnetwork.zombies.equipment.gun.shoot.GunHit;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Finds targets for a shot.
 * While entity finders find possible entity candidates, implementations of this class
 * should find actual targets: both regular shots and headshots.
 */
public interface TargetFinder {

    /**
     * The result of a target finding.
     * @param regular A {@link Collection} of regular {@link GunHit}s
     * @param headshot A {@link Collection} of {@link GunHit}s that should be considered "headshots"
     */
    record Result(@NotNull Collection<GunHit> regular, @NotNull Collection<GunHit> headshot) {

        /**
         * Creates a {@link Result}.
         * @param regular A {@link Collection} of regular {@link GunHit}s
         * @param headshot A {@link Collection} of {@link GunHit}s that should be considered "headshots"
         */
        public Result {
            Objects.requireNonNull(regular, "regular");
            Objects.requireNonNull(headshot, "headshot");
        }

    }

    /**
     * Finds the target of a shot.
     * @param shooter The {@link Entity} that shot
     * @param start The start position of the shot
     * @param end The endpoint of the shot
     * @param previousHits A {@link Collection} of {@link UUID}s of previous targets
     * @return The {@link Result} of the target finding
     */
    @NotNull Result findTarget(@NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
                               @NotNull Collection<UUID> previousHits);

}
