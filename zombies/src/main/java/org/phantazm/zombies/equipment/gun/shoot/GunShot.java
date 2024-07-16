package org.phantazm.zombies.equipment.gun.shoot;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.shoot.fire.Firer;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents an individual gun shot. This should be produced by {@link Firer} implementations. As such, it is
 * associated with a gun's individual fire, not a complete volley of shots.
 *
 * @param start   The start position of the shot
 * @param end     The end position of the shot
 * @param gunHits The GunHits
 */
public record GunShot(
    @NotNull Pos start,
    @NotNull Point end,
    @NotNull Collection<GunHit> gunHits) {

    public GunShot {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        Objects.requireNonNull(gunHits);
    }
}
