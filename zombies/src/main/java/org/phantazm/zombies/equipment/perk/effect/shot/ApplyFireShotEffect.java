package org.phantazm.zombies.equipment.perk.effect.shot;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.element.core.annotation.document.Description;
import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

@Description("""
    An entity action that sets an entity on fire for a configurable amount of time, health, and damage interval.
    """)
@Model("zombies.perk.effect.shot_entity.apply_fire")
@Cache(false)
public class ApplyFireShotEffect implements ShotEffect, Tickable {
    private final Data data;
    private final Tag<Long> lastDamageTicksTag;
    private final Deque<DamageTarget> activeEntities;

    @FactoryMethod
    public ApplyFireShotEffect(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);

        UUID uuid = UUID.randomUUID();
        this.lastDamageTicksTag = Tag.Long("last_fire_damage_ticks_" + uuid).defaultValue(-1L);

        this.activeEntities = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void perform(@NotNull Entity entity, @NotNull ZombiesPlayer zombiesPlayer) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            //can't set non-LivingEntity on fire as they have no health
            return;
        }

        if (!(livingEntity instanceof Mob mob)) {
            return;
        }

        if (mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_FIRE)) {
            return;
        }

        livingEntity.setFireForDuration(data.fireTicks);

        boolean alreadyActive = entity.getTag(lastDamageTicksTag) != -1;
        entity.setTag(lastDamageTicksTag, 0L);

        if (!alreadyActive) {
            zombiesPlayer.getPlayer().ifPresent(player -> {
                activeEntities.add(new DamageTarget(player, livingEntity));
            });
        }
    }

    @Override
    public void tick(long time) {
        activeEntities.removeIf(target -> {
            LivingEntity entity = target.target;

            if (entity.isRemoved() || entity.isDead() || !entity.isOnFire()) {
                stopFire(entity);
                return true;
            }

            long lastDamageTicks = entity.getTag(this.lastDamageTicksTag);
            entity.setTag(this.lastDamageTicksTag, ++lastDamageTicks);
            if (lastDamageTicks >= data.damageInterval) {
                doDamage(entity, target.damager);
                entity.setTag(this.lastDamageTicksTag, 0L);
            }

            return false;
        });
    }

    private void doDamage(LivingEntity entity, Entity damager) {
        Damage damage = new Damage(DamageType.ON_FIRE, null, damager, null, data.damage);
        entity.damage(damage, data.bypassArmor);
    }

    private void stopFire(Entity entity) {
        entity.removeTag(lastDamageTicksTag);
    }

    private record DamageTarget(Entity damager,
        LivingEntity target) {
    }

    @DataObject
    public record Data(
        @Description("The number of ticks the hit entity will be set on fire") int fireTicks,
        @Description("The number of ticks between fire damage applications") int damageInterval,
        @Description("The amount of damage dealt on each application") float damage,
        @Description("Whether fire damage should bypass armor damage reduction") boolean bypassArmor) {
    }
}
