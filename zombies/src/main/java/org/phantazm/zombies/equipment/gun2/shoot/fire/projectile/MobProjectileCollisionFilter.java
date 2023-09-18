package org.phantazm.zombies.equipment.gun2.shoot.fire.projectile;

import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.player.PlayerComponent;
import org.phantazm.zombies.player.ZombiesPlayer;

public class MobProjectileCollisionFilter implements PlayerComponent<ProjectileCollisionFilter> {

    private static final ProjectileCollisionFilter FILTER = cause -> cause instanceof Mob;

    @Override
    public @NotNull ProjectileCollisionFilter forPlayer(@NotNull ZombiesPlayer player, @NotNull InjectionStore injectionStore) {
        return FILTER;
    }
}
