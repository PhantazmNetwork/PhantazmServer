package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.Flags;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.zombies.event.EntityDamageByGunEvent;
import org.phantazm.zombies.map.objects.MapObjects;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that deals damage to targets.
 */
@Model("zombies.gun.shot_handler.damage")
@Cache
public class DamageShotHandler implements ShotHandler {

    private final Data data;
    private final MapObjects mapObjects;
    private final MobStore mobStore;

    /**
     * Creates a new {@link DamageShotHandler} with the given {@link Data}.
     *
     * @param data The {@link Data} to use
     */
    @FactoryMethod
    public DamageShotHandler(@NotNull Data data, @NotNull MapObjects mapObjects, @NotNull MobStore mobStore) {
        this.data = Objects.requireNonNull(data, "data");
        this.mapObjects = Objects.requireNonNull(mapObjects, "mapObjects");
        this.mobStore = Objects.requireNonNull(mobStore, "mobStore");
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
            @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        handleDamageTargets(gun, attacker, shot.regularTargets(), data.damage, false);
        handleDamageTargets(gun, attacker, shot.headshotTargets(), data.headshotDamage, true);
    }

    private void handleDamageTargets(Gun gun, Entity attacker, Collection<GunHit> targets, float damage,
            boolean headshot) {
        boolean hasInstakill = mapObjects.module().flaggable().hasFlag(Flags.INSTA_KILL);

        for (GunHit target : targets) {
            LivingEntity targetEntity = target.entity();
            PhantazmMob mob = mobStore.getMob(targetEntity.getUuid());
            boolean resistInstaKill =
                    mob != null && mob.model().getMetaNode().getBooleanOrDefault(false, "resistInstaKill");

            if (hasInstakill && !resistInstaKill) {
                EntityDamageByGunEvent event =
                        new EntityDamageByGunEvent(gun, targetEntity, attacker, headshot, true, damage);
                EventDispatcher.call(event);
                if (!event.isCancelled()) {
                    targetEntity.kill();
                }

                continue;
            }

            EntityDamageByGunEvent event =
                    new EntityDamageByGunEvent(gun, targetEntity, attacker, headshot, false, damage);
            EventDispatcher.call(event);

            if (!event.isCancelled()) {
                targetEntity.damage(DamageType.fromEntity(attacker), damage);
            }
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    /**
     * Data for a {@link DamageShotHandler}.
     *
     * @param damage         The amount of damage to deal to regular targets
     * @param headshotDamage The amount of damage to deal to headshots
     */
    @DataObject
    public record Data(float damage, float headshotDamage) {

    }

}
