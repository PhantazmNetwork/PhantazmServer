package org.phantazm.zombies.mob.skill.hit_action;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.mob.PhantazmMob;

@Model("zombies.mob.skill.projectile.hit_action.damage")
@Cache
public class DamageAction implements ProjectileHitEntityAction {
    private final Data data;

    @FactoryMethod
    public DamageAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public void perform(@Nullable PhantazmMob shooter, @NotNull Entity projectile, @NotNull Entity target) {
        if (!(target instanceof LivingEntity livingEntity)) {
            return;
        }

        if (livingEntity.damage(
                Damage.fromProjectile(shooter == null ? null : shooter.entity(), projectile, data.damage),
                data.bypassArmor)) {
            double yaw = Math.toRadians(projectile.getPosition().yaw());
            livingEntity.takeKnockback(data.knockback, Math.sin(yaw), -Math.cos(yaw));
        }
    }

    @DataObject
    public record Data(float damage, float knockback, boolean bypassArmor) {
        @Default("bypassArmor")
        public static @NotNull ConfigElement defaultBypassArmor() {
            return ConfigPrimitive.of(false);
        }

        @Default("knockback")
        public static @NotNull ConfigElement defaultKnockback() {
            return ConfigPrimitive.of(0.4F);
        }
    }
}
