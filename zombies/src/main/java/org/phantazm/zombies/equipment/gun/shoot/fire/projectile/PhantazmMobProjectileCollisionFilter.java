package org.phantazm.zombies.equipment.gun.shoot.fire.projectile;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;

@Model("zombies.gun.firer.projectile.collision_filter.phantazm_mob")
@Cache(false)
public class PhantazmMobProjectileCollisionFilter implements ProjectileCollisionFilter {
    @FactoryMethod
    public PhantazmMobProjectileCollisionFilter() {

    }

    @Override
    public boolean shouldExplode(@NotNull Entity cause) {
        return cause instanceof Mob;
    }

}
