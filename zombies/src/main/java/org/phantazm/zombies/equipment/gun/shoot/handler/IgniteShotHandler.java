package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A {@link ShotHandler} that sets {@link Entity}s on fire.
 */
@Model("zombies.gun.shot_handler.ignite")
@Cache
public class IgniteShotHandler implements ShotHandler {

    private final Data data;

    private final Tag<Long> lastFireDamage;
    private final Deque<LivingEntity> targets;

    /**
     * Creates an {@link IgniteShotHandler}.
     *
     * @param data The {@link IgniteShotHandler}'s {@link Data}
     */
    @FactoryMethod
    public IgniteShotHandler(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");

        UUID uuid = UUID.randomUUID();
        this.lastFireDamage = Tag.Long("last_fire_damage_time" + uuid).defaultValue(-1L);
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
            entity.setFireForDuration(duration);

            boolean alreadyActive = entity.getTag(lastFireDamage) != -1;
            entity.setTag(lastFireDamage, System.currentTimeMillis());

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

            if ((time - target.getTag(this.lastFireDamage)) / MinecraftServer.TICK_MS >= data.damageInterval) {
                damage(target);
                target.setTag(this.lastFireDamage, time);
            }

            return false;
        });
    }

    private void remove(LivingEntity target) {
        target.removeTag(lastFireDamage);
    }

    private void damage(LivingEntity target) {
        target.damage(DamageType.ON_FIRE, data.damage, data.bypassArmor);
    }

    @DataObject
    public record Data(int normalFireTicks,
                       int headshotFireTicks,
                       int damageInterval,
                       float damage,
                       boolean bypassArmor) {

    }

}
