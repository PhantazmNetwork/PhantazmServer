package org.phantazm.zombies.equipment.gun.shoot.handler;

import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.effect.GunTickEffect;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.UUID;

/**
 * Handler for a gun's shot.
 */
public interface ShotHandler extends GunTickEffect {

    /**
     * Handles a shot.
     *
     * @param state        The state of the gun
     * @param attacker     The {@link Entity} that shot
     * @param previousHits The gun's previously hit targets
     * @param shot         The shot that was fired
     */
    void handle(@NotNull GunState state, @NotNull Entity attacker, @NotNull Collection<UUID> previousHits,
            @NotNull GunShot shot);

}
