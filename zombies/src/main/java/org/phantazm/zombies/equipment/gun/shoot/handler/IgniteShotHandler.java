package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A {@link ShotHandler} that sets {@link Entity}s on fire.
 */
@Model("zombies.gun.shot_handler.ignite")
@Cache(false)
public class IgniteShotHandler implements ShotHandler {
    private final Data data;

    private final Tag<Long> lastFireDamageTicksTag;
    private final Deque<LivingEntity> targets;

    /**
     * Creates an {@link IgniteShotHandler}.
     *
     * @param data The {@link IgniteShotHandler}'s {@link Data}
     */
    @FactoryMethod
    public IgniteShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);

        UUID uuid = UUID.randomUUID();
        this.lastFireDamageTicksTag = Tag.Long("last_fire_damage_ticks" + uuid).defaultValue(-1L);
        this.targets = new ConcurrentLinkedDeque<>();
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        setFire(shot.regularTargets(), data.normalFireTicks);
        setFire(shot.headshotTargets(), data.headshotFireTicks);
    }

    private void setFire(Collection<GunHit> hits, int duration) {
        for (GunHit target : hits) {
            LivingEntity entity = target.entity();
            if (!(entity instanceof Mob mob)) {
                return;
            }

            if (mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_FIRE)) {
                continue;
            }

            entity.setFireForDuration(duration);

            long lastFireDamageTicks = entity.getTag(lastFireDamageTicksTag);
            boolean alreadyActive = lastFireDamageTicks != -1;
            entity.setTag(lastFireDamageTicksTag, ++lastFireDamageTicks);

            if (!alreadyActive) {
                targets.add(entity);
            }
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {
        targets.removeIf(target -> {
            if (target.isDead() || target.isRemoved() || !target.isOnFire()) {
                remove(target);
                return true;
            }

            if (target.getTag(this.lastFireDamageTicksTag) >= data.damageInterval) {
                damage(target);
                target.setTag(this.lastFireDamageTicksTag, 0L);
            }

            return false;
        });
    }

    private void remove(LivingEntity target) {
        target.removeTag(lastFireDamageTicksTag);
    }

    private void damage(LivingEntity target) {
        target.damage(DamageType.ON_FIRE, data.damage, data.bypassArmor);
    }

    @DataObject
    public record Data(
        int normalFireTicks,
        int headshotFireTicks,
        int damageInterval,
        float damage,
        boolean bypassArmor) {

    }

}
