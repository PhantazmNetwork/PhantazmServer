package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.ExtraNodeKeys;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.critical_hits")
@Cache
public class CriticalHitsUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public CriticalHitsUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<EntityDamageByGunEvent> event = EventListener.builder(EntityDamageByGunEvent.class)
                    .handler(this::handleMobShot).build();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void handleMobShot(EntityDamageByGunEvent event) {
                    if (!event.shooter().getUuid().equals(zombiesPlayer.getUUID())) {
                        return;
                    }

                    if (!event.isHeadshot() && data.headshotRequired) {
                        return;
                    }

                    if (Math.random() >= data.critChance) {
                        return;
                    }

                    if (Math.random() >= data.instakillChance) {
                        event.setDamage((float) (event.getDamage() * data.critDamageFactor));
                        return;
                    }

                    if (event.getEntity() instanceof Mob mob &&
                        mob.data().extra().getBooleanOrDefault(ExtraNodeKeys.RESIST_INSTAKILL, false)) {
                        event.setDamage((float) (event.getDamage() * data.instakillDamageFactor));
                    } else {
                        event.setInstakill(true);
                    }
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @Default("""
        {
          headshotRequired=true
        }
        """)
    @DataObject
    public record Data(double critChance,
        double critDamageFactor,
        double instakillChance,
        double instakillDamageFactor,
        boolean headshotRequired) {

    }
}
