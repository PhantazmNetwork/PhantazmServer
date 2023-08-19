package org.phantazm.zombies.equipment.gun.shoot.fire.projectile;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;

import java.util.Objects;

/**
 * A {@link ProjectileCollisionFilter} which explodes when it comes in contact with {@link PhantazmMob}s.
 */
@Model("zombies.gun.firer.projectile.collision_filter.phantazm_mob")
@Cache(false)
public class PhantazmMobProjectileCollisionFilter implements ProjectileCollisionFilter {
    private final MobStore mobStore;

    /**
     * Creates a new {@link PhantazmMobProjectileCollisionFilter}.
     *
     * @param mobStore A {@link MobStore} to retrieve {@link PhantazmMob}s from
     */
    @FactoryMethod
    public PhantazmMobProjectileCollisionFilter(@NotNull MobStore mobStore) {
        this.mobStore = Objects.requireNonNull(mobStore);
    }

    @Override
    public boolean shouldExplode(@NotNull Entity cause) {
        return mobStore.getMob(cause.getUuid()) != null;
    }

}
