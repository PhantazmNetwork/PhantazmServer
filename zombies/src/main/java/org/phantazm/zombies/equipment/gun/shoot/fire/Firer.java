package org.phantazm.zombies.equipment.gun.shoot.fire;

import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.effect.GunTickEffect;
import org.phantazm.zombies.equipment.gun.shoot.handler.ShotHandler;

import java.util.Collection;
import java.util.UUID;

/**
 * Fires shots for a gun.
 */
public interface Firer extends GunTickEffect {

    /**
     * Fires a shot.
     *
     * @param state        The {@link GunState} of the gun
     * @param start        The start position of the shot
     * @param previousHits Previously hit entities by other {@link Firer}s
     */
    void fire(@NotNull GunState state, @NotNull Pos start, @NotNull Collection<UUID> previousHits);

    void addExtraShotHandler(ShotHandler shotHandler);

    void removeExtraShotHandler(ShotHandler shotHandler);

}
