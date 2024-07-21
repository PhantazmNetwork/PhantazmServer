package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.event.player.ZombiesPlayerKillMobEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.heal_on_headshot_kill")
@Cache
public class HealOnHeadshotKillUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public HealOnHeadshotKillUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerKillMobEvent> event = EventListener.builder(ZombiesPlayerKillMobEvent.class)
                    .handler(this::onMobKill).build();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void onMobKill(ZombiesPlayerKillMobEvent event) {
                    if (event.getZombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    Damage damage = event.lastDamageSource();
                    if (!damage.getTag(Tags.HEADSHOT_TAG)) {
                        return;
                    }

                    Mob target = event.target();
                    if (target.data().tags().contains(data.specialTag)) {
                        event.getPlayer().getAcquirable().sync(entity -> {
                            Player player = (Player) entity;
                            player.setHealth((float) (player.getHealth() + data.specialHealAmount));
                        });
                    } else {
                        event.getPlayer().getAcquirable().sync(entity -> {
                            Player player = (Player) entity;
                            player.setHealth((float) (player.getHealth() + data.healAmount));
                        });
                    }
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }

    @DataObject
    public record Data(double healAmount,
        double specialHealAmount,
        @NotNull Key specialTag) {

    }
}
