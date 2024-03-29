package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventDispatcher;
import org.jetbrains.annotations.NotNull;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.equipment.gun.Gun;
import org.phantazm.zombies.equipment.gun.GunState;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.gun.shoot.GunShot;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * A {@link ShotHandler} that deals damage to targets. Raises {@link EntityDamageByGunEvent}s on a successful hit.
 */
@Model("zombies.gun.shot_handler.damage")
@Cache(false)
public class DamageShotHandler implements ShotHandler {
    private final Data data;
    private final ZombiesScene zombiesScene;

    /**
     * Creates a new {@link DamageShotHandler} with the given {@link Data}.
     *
     * @param data The {@link Data} to use
     */
    @FactoryMethod
    public DamageShotHandler(@NotNull Data data, @NotNull ZombiesScene zombiesScene) {
        this.data = Objects.requireNonNull(data);
        this.zombiesScene = Objects.requireNonNull(zombiesScene);
    }

    @Override
    public void handle(@NotNull Gun gun, @NotNull GunState state, @NotNull Entity attacker,
        @NotNull Collection<UUID> previousHits, @NotNull GunShot shot) {
        handleDamageTargets(gun, attacker, shot.regularTargets(), data.damage, false);
        handleDamageTargets(gun, attacker, shot.headshotTargets(), data.headshotDamage, true);
    }

    private void handleDamageTargets(Gun gun, Entity attacker, Collection<GunHit> targets, float damage,
        boolean headshot) {
        for (GunHit target : targets) {
            LivingEntity targetEntity = target.entity();

            EntityDamageByGunEvent event =
                new EntityDamageByGunEvent(gun, targetEntity, attacker, headshot, false, damage);
            zombiesScene.broadcastEvent(event);

            if (event.isCancelled()) {
                continue;
            }

            targetEntity.getAcquirable().sync(ignored -> {
                float actualDamage = event.getDamage();
                if (event.isInstakill()) {
                    targetEntity.damage(Damage.fromEntity(attacker, targetEntity.getHealth()), true);
                    return;
                }

                if (attacker instanceof LivingEntity livingEntity) {
                    actualDamage *= livingEntity.getAttributeValue(Attributes.DAMAGE_MULTIPLIER);
                }

                if (headshot) {
                    actualDamage *= target.entity().getAttributeValue(Attributes.HEADSHOT_DAMAGE_MULTIPLIER);
                }

                Damage damageType = Damage.fromEntity(attacker, actualDamage);
                switch (data.armorBehavior) {
                    case ALWAYS_BYPASS -> targetEntity.damage(damageType, true);
                    case NEVER_BYPASS -> targetEntity.damage(damageType, false);
                    case BYPASS_ON_HEADSHOT -> targetEntity.damage(damageType, headshot);
                    case BYPASS_ON_NON_HEADSHOT -> targetEntity.damage(damageType, !headshot);
                }
            });
        }
    }

    @Override
    public void tick(@NotNull GunState state, long time) {

    }

    public enum ArmorBehavior {
        ALWAYS_BYPASS,
        NEVER_BYPASS,
        BYPASS_ON_HEADSHOT,
        BYPASS_ON_NON_HEADSHOT
    }

    /**
     * Data for a {@link DamageShotHandler}.
     *
     * @param damage         The amount of damage to deal to regular targets
     * @param headshotDamage The amount of damage to deal to headshots
     */
    @DataObject
    public record Data(float damage,
        float headshotDamage,
        @NotNull ArmorBehavior armorBehavior) {

    }
}
