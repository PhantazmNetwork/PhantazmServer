package org.phantazm.zombies.powerup.action.component;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
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

    @DataObject
    public record Data(
        double radius,
        @NotNull Key modifier,
        int coinsPerKill,
        @NotNull BossDamageType bossDamageType,
        float bossDamage,
        boolean bypassArmor) {
        @Default("bossDamageType")
        public static @NotNull ConfigElement defaultBossDamageType() {
            return ConfigPrimitive.of("HEALTH_FACTOR");
        }

        @Default("bossDamage")
        public static @NotNull ConfigElement defaultBossDamage() {
            return ConfigPrimitive.of(0.25F);
        }

        @Default("bypassArmor")
        public static @NotNull ConfigElement defaultBypassArmor() {
            return ConfigPrimitive.of(true);
        }
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

            instance.getEntityTracker()
                .nearbyEntities(powerup.spawnLocation(), data.radius, EntityTracker.Target.LIVING_ENTITIES,
                    entity -> {
                        if (!(entity instanceof Mob)) {
                            return;
                        }

                        entity.getAcquirable().sync(self -> {
                            Mob mob = (Mob) self;
                            if (mob.data().extra().getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL)) {
                                switch (data.bossDamageType) {
                                    case HEALTH_FACTOR -> mob.damage(Damage.fromPlayer(player, mob
                                        .getMaxHealth() * data.bossDamage), data.bypassArmor);
                                    case CONSTANT -> mob.damage(Damage.fromPlayer(player, data.bossDamage),
                                        data.bypassArmor);
                                }

                                if (mob.getHealth() <= 0) {
                                    giveCoins(zombiesPlayer);
                                }

                                return;
                            }

                            giveCoins(zombiesPlayer);
                            mob.damage(Damage.fromPlayer(player, mob.getHealth()), data.bypassArmor);
                        });
                    });
        }

        private void giveCoins(ZombiesPlayer zombiesPlayer) {
            PlayerCoins coins = zombiesPlayer.module().getCoins();

            TransactionResult result = coins.runTransaction(
                new Transaction(zombiesPlayer.module().compositeTransactionModifiers().modifiers(data.modifier),
                    data.coinsPerKill));
            result.applyIfAffordable(coins);
        }
    }
}
