package org.phantazm.zombies.equipment.gun.target;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * Finds targets for a shot.
 * Entity finders find entities that are possible target candidates.
 * Implementations may use these in order to select entities to become actual targets.
 * These targets are grouped into "regular" and "headshot" targets.
 * Targets do not necessarily need to be shot through the head in order to be considered headshots, but
 * other code treats regular shots and headshots differently.
 */
public interface TargetFinder {

    /**
     * Finds the target of a shot.
     *
     * @param shooter      The {@link Entity} that shot
     * @param start        The start position of the shot
     * @param end          The endpoint of the shot
     * @param previousHits A {@link Collection} of {@link UUID}s of previous targets
     * @return The {@link Result} of the target finding
     */
    @NotNull Result findTarget(@NotNull Entity shooter, @NotNull Pos start, @NotNull Point end,
            @NotNull Collection<UUID> previousHits);

    /**
     * The result of a target finding.
     *
     * @param regular  A {@link Collection} of regular {@link GunHit}s
     * @param headshot A {@link Collection} of {@link GunHit}s that should be considered "headshots"
     */
    record Result(@NotNull Collection<GunHit> regular, @NotNull Collection<GunHit> headshot) {

        /**
         * Creates a {@link Result}.
         *
         * @param regular  A {@link Collection} of regular {@link GunHit}s
         * @param headshot A {@link Collection} of {@link GunHit}s that should be considered "headshots"
         */
        public Result {
            Objects.requireNonNull(regular, "regular");
            Objects.requireNonNull(headshot, "headshot");
        }

    }

}
