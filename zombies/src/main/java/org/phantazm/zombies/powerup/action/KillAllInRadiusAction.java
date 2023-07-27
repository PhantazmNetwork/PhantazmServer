package org.phantazm.zombies.powerup.action;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.PhantazmMob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.Transaction;
import org.phantazm.zombies.coin.TransactionResult;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.powerup.Powerup;

import java.util.function.Supplier;

@Model("zombies.powerup.action.kill_all_in_radius")
@Cache(false)
public class KillAllInRadiusAction implements Supplier<PowerupAction> {
    private final Data data;
    private final Instance instance;
    private final MobStore mobStore;

    @FactoryMethod
    public KillAllInRadiusAction(@NotNull Data data, @NotNull Instance instance, @NotNull MobStore mobStore) {
        this.data = data;
        this.instance = instance;
        this.mobStore = mobStore;
    }

    @Override
    public PowerupAction get() {
        return new Action(data, instance, mobStore);
    }

    @DataObject
    public record Data(double radius, @NotNull Key modifier, int coinsPerKill) {

    }

    private static class Action extends InstantAction {
        private final Data data;
        private final Instance instance;
        private final MobStore mobStore;

        private Action(Data data, Instance instance, MobStore mobStore) {
            this.instance = instance;
            this.data = data;
            this.mobStore = mobStore;
        }

        @Override
        public void activate(@NotNull Powerup powerup, @NotNull ZombiesPlayer player, long time) {
            instance.getEntityTracker()
                    .nearbyEntitiesUntil(powerup.spawnLocation(), data.radius, EntityTracker.Target.LIVING_ENTITIES,
                            entity -> {
                                PhantazmMob mob = mobStore.getMob(entity.getUuid());
                                if (mob != null && !mob.model().getExtraNode()
                                        .getBooleanOrDefault(false, ExtraNodeKeys.RESIST_INSTAKILL)) {
                                    PlayerCoins coins = player.module().getCoins();

                                    TransactionResult result = coins.runTransaction(new Transaction(
                                            player.module().compositeTransactionModifiers().modifiers(data.modifier),
                                            data.coinsPerKill));

                                    if (result.applyIfAffordable(coins)) {
                                        entity.setTag(Tags.LAST_HIT_BY, player.getUUID());
                                        entity.kill();
                                    }
                                    else {
                                        return true;
                                    }
                                }

                                return false;
                            });
        }
    }
}
