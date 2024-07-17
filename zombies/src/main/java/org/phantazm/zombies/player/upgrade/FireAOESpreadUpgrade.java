package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.entity.Entity;
import net.minestom.server.event.EventListener;
import net.minestom.server.instance.EntityTracker;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.equipment.gun.shoot.GunHit;
import org.phantazm.zombies.equipment.perk.effect.shot.ApplyFireShotEffect;
import org.phantazm.zombies.event.equipment.EntitiesHitByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.fire_aoe_spread")
@Cache
public class FireAOESpreadUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public FireAOESpreadUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<EntitiesHitByGunEvent> event = EventListener.builder(EntitiesHitByGunEvent.class)
                    .handler(this::handleMobShot).build();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void handleMobShot(EntitiesHitByGunEvent event) {
                    if (!event.getShooter().getUuid().equals(zombiesPlayer.getUUID())) {
                        return;
                    }

                    ApplyFireShotEffect applyFire = zombiesPlayer.lookupShotEffect(ApplyFireShotEffect.class);
                    if (applyFire == null) {
                        return;
                    }

                    for (GunHit hit : event.targets()) {
                        Entity hitEntity = hit.entity();
                        zombiesPlayer.getScene().instance().getEntityTracker().nearbyEntities(hitEntity.getPosition(),
                            data.radius, EntityTracker.Target.LIVING_ENTITIES, target -> {
                                if (target == event.getShooter() || target == hitEntity || !(target instanceof Mob)) {
                                    return;
                                }

                                applyFire.perform(target, zombiesPlayer);
                            });
                    }
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @DataObject
    public record Data(double radius) {

    }
}
