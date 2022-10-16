package com.github.phantazmnetwork.zombies.equipment.gun.shoot.fire.projectile;

import com.github.phantazmnetwork.mob.MobStore;
import com.github.phantazmnetwork.mob.PhantazmMob;
import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A {@link ProjectileCollisionFilter} which explodes when it comes in contact with {@link PhantazmMob}s.
 */
@Model("zombies.gun.firer.projectile.collision_filter.phantazm_mob")
public class PhantazmMobProjectileCollisionFilter implements ProjectileCollisionFilter {

    private final MobStore mobStore;

    /**
     * Creates a new {@link PhantazmMobProjectileCollisionFilter}.
     *
     * @param mobStore A {@link MobStore} to retrive {@link PhantazmMob}s from
     */
    @FactoryMethod
    public PhantazmMobProjectileCollisionFilter(
            @NotNull @Dependency("zombies.dependency.mob.store") MobStore mobStore) {
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public boolean shouldExplode(@NotNull Entity cause) {
        return mobStore.getMob(cause.getUuid()) != null;
    }

}
