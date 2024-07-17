package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.MathUtils;
import org.phantazm.core.AttributeUtils;
import org.phantazm.core.DamageUtils;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.powerup.action.InstantAction;
import org.phantazm.zombies.powerup.action.PowerupAction;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Optional;

@Model("zombies.powerup.action.kill_all_in_radius")
@Cache(false)
public class KillAllInRadiusAction implements PowerupActionComponent {
    private final Data data;

    @FactoryMethod
    public KillAllInRadiusAction(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PowerupAction apply(@NotNull ZombiesScene scene) {
        return new Action(data, scene.instance());
    }

    public enum BossDamageType {
        HEALTH_FACTOR,
        CONSTANT
    }

    private static class Action extends InstantAction {
        private final Data data;
        private final Instance instance;

        private Action(Data data, Instance instance) {
            this.instance = instance;
            this.data = data;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer zombiesPlayer, long time) {
            Optional<Player> playerOptional = zombiesPlayer.getPlayer();
            if (playerOptional.isEmpty()) {
                return;
            }

            Player player = playerOptional.get();
            double instakillRadius = AttributeUtils.computeWithBase((float) data.radius,
                player.getAttribute(Attributes.POWERUP_KILL_ALL_IN_RADIUS_INSTAKILL_DISTANCE_SCALING));

            double damageRadiusStart = AttributeUtils.computeWithBase((float) data.damageRadiusStart,
                player.getAttribute(Attributes.POWERUP_KILL_ALL_IN_RADIUS_DAMAGE_DISTANCE_START_SCALING));

            double damageRadiusEnd = AttributeUtils.computeWithBase((float) data.damageRadiusEnd,
                player.getAttribute(Attributes.POWERUP_KILL_ALL_IN_RADIUS_DAMAGE_DISTANCE_END_SCALING));

            instance.getEntityTracker()
                .nearbyEntities(powerup.spawnLocation(), instakillRadius, EntityTracker.Target.LIVING_ENTITIES,
                    entity -> {
                        if (!(entity instanceof Mob)) {
                            return;
                        }

                        entity.getAcquirable().sync(self -> {
                            Mob mob = (Mob) self;
                            if (mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.RESIST_INSTAKILL, false)) {
                                switch (data.bossDamageType) {
                                    case HEALTH_FACTOR ->
                                        DamageUtils.damage(data.damageType, mob, player, mob.getMaxHealth() * data.bossDamage, data.bypassArmor);
                                    case CONSTANT ->
                                        DamageUtils.damage(data.damageType, mob, player, data.bossDamage, data.bypassArmor);
                                }

                                if (mob.getHealth() <= 0) {
                                    giveCoins(zombiesPlayer);
                                }

                                return;
                            }

                            giveCoins(zombiesPlayer);
                            mob.damage(Damage.fromPlayer(player, mob.getHealth()));
                        });
                    });

            if (damageRadiusEnd > 0) {
                instance.getEntityTracker().nearbyEntities(powerup.spawnLocation(), damageRadiusEnd,
                    EntityTracker.Target.LIVING_ENTITIES, livingEntity -> {
                        if (!(livingEntity instanceof Mob) || livingEntity.isDead()) {
                            return;
                        }

                        double distanceSquared = powerup.spawnLocation().distanceSquared(livingEntity.getPosition());
                        if (distanceSquared < damageRadiusStart * damageRadiusStart) {
                            return;
                        }

                        double actualDistance = Math.sqrt(distanceSquared);
                        double offset = actualDistance - damageRadiusStart;
                        if (offset < 0 || damageRadiusStart + offset > damageRadiusEnd) {
                            // sanity check
                            return;
                        }

                        double difference = damageRadiusEnd - damageRadiusStart;
                        double lerpScale = MathUtils.clamp(offset / difference, 0, 1);
                        double damage = data.damageStart + lerpScale * (data.damageEnd - data.damageStart);

                        livingEntity.getAcquirable().sync(self -> {
                            Mob mob = (Mob) self;
                            DamageUtils.damage(data.damageType, mob, player, (float) damage, data.bypassArmor);
                        });
                    });
            }
        }

        private void giveCoins(ZombiesPlayer zombiesPlayer) {
            PlayerCoins coins = zombiesPlayer.module().getCoins();

            TransactionResult result = coins.runTransaction(
                new Transaction(zombiesPlayer.module().compositeTransactionModifiers().modifiers(data.modifier),
                    data.coinsPerKill));
            result.applyIfAffordable(coins);
        }
    }

    @Default("""
        {
          damageRadiusStart=0.0,
          damageRadiusEnd=0.0,
          damageStart=0.0,
          damageEnd=0.0,
          bossDamageType='HEALTH_FACTOR',
          bossDamage=0.25F,
          bypassArmor=true,
          damageType=null
        }
        """)
    @DataObject
    public record Data(
        double radius,
        double damageRadiusStart,
        double damageRadiusEnd,
        double damageStart,
        double damageEnd,
        @NotNull Key modifier,
        int coinsPerKill,
        @NotNull BossDamageType bossDamageType,
        float bossDamage,
        boolean bypassArmor,
        String damageType) {
    }
}
