package org.phantazm.zombies.equipment.gun.shoot.handler;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.core.AttributeUtils;
import org.phantazm.core.DamageUtils;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.Tags;
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
        handleDamageTargets(gun, attacker, shot.gunHits(), data.damage, data.headshotDamage);
    }

    private void handleDamageTargets(Gun gun, Entity attacker, Collection<GunHit> targets, float damageAmount, float headshotDamageAmount) {
        for (GunHit target : targets) {
            LivingEntity targetEntity = target.entity();
            boolean headshot = target.isHeadshot();

            float finalDamage = Math.max(0, computeDamageAmount(attacker, targetEntity, headshot, damageAmount,
                headshotDamageAmount));
            if (finalDamage == 0) {
                continue;
            }

            EntityDamageByGunEvent event =
                new EntityDamageByGunEvent(gun, targetEntity, attacker, headshot, false, finalDamage);
            zombiesScene.broadcastEvent(event);

            if (event.isCancelled()) {
                continue;
            }

            targetEntity.getAcquirable().sync(ignored -> {
                float baseDamage = event.damageAmount();
                if (event.isInstakill()) {
                    targetEntity.damage(Damage.fromEntity(attacker, targetEntity.getHealth()));
                    return;
                }

                switch (data.armorBehavior) {
                    case ALWAYS_BYPASS -> DamageUtils.damage(data.damageType, targetEntity,
                        amount -> makeDamage(attacker, amount, headshot), baseDamage, true);

                    case NEVER_BYPASS -> DamageUtils.damage(data.damageType, targetEntity,
                        amount -> makeDamage(attacker, amount, headshot), baseDamage, false);

                    case BYPASS_ON_HEADSHOT -> DamageUtils.damage(data.damageType, targetEntity,
                        amount -> makeDamage(attacker, amount, headshot), baseDamage, headshot);

                    case BYPASS_ON_NON_HEADSHOT -> DamageUtils.damage(data.damageType, targetEntity,
                        amount -> makeDamage(attacker, amount, headshot), baseDamage, !headshot);
                }
            });
        }
    }

    private static float computeDamageAmount(Entity attacker, LivingEntity target, boolean headshot, float amount, float headshotAmount) {
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            return headshot ? AttributeUtils.computeWithBase(headshotAmount,
                target.getAttribute(Attributes.HEADSHOT_DAMAGE_RECEIVED)) : amount;
        }

        float baseDamage = AttributeUtils.computeWithBase(headshotAmount, livingAttacker.getAttribute(Attributes.GUN_DAMAGE));
        if (!headshot) {
            return baseDamage;
        }

        return AttributeUtils.computeWithBase(baseDamage, target.getAttribute(Attributes.HEADSHOT_DAMAGE_RECEIVED));
    }

    private static Damage makeDamage(Entity attacker, float amount, boolean headshot) {
        Damage damage = new Damage(DamageType.MOB_ATTACK, attacker, attacker, attacker.getPosition(), amount);
        if (headshot) damage.setTag(Tags.HEADSHOT_TAG, true);
        return damage;
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
    @Default("""
        {
          damageType=null
        }
        """)
    @DataObject
    public record Data(float damage,
        float headshotDamage,
        @NotNull ArmorBehavior armorBehavior,
        @Nullable String damageType) {

    }
}
