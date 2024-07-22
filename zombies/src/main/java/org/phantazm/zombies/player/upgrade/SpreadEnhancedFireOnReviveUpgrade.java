package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.EntityTracker;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.Cooldown;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.equipment.perk.effect.shot.ApplyFireShotEffect;
import org.phantazm.zombies.event.player.ZombiesPlayerReviveEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.spread_enhanced_fire_on_revive")
@Cache
public class SpreadEnhancedFireOnReviveUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public SpreadEnhancedFireOnReviveUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerReviveEvent> event = EventListener.builder(ZombiesPlayerReviveEvent.class)
                    .handler(this::handleRevive).build();
                private final Cooldown cooldown = Cooldown.cooldown();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void tick(long time) {
                    cooldown.step();
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void handleRevive(ZombiesPlayerReviveEvent event) {
                    if (event.zombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    ApplyFireShotEffect applyFire = zombiesPlayer.lookupShotEffect(ApplyFireShotEffect.class);
                    if (applyFire == null) {
                        return;
                    }

                    Entity reviver = event.getEntity();
                    Instance instance = reviver.getInstance();
                    if (instance == null || !cooldown.takeCooldown(data.cooldown)) {
                        return;
                    }

                    instance.getEntityTracker().nearbyEntities(reviver.getPosition(), data.radius,
                        EntityTracker.Target.LIVING_ENTITIES, entity -> {
                            if (!(entity instanceof Mob mob)) {
                                return;
                            }

                            applyFire.perform(mob, zombiesPlayer, data.scale);
                        });
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return true;
        }
    }

    @DataObject
    public record Data(double radius,
        int cooldown,
        double scale) {

    }
}
