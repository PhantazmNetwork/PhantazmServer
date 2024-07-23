package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.key.Key;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.mob2.Mob;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Set;

@Model("zombies.upgrade.targeted_damage_increase")
@Cache
public class TargetedDamageIncreaseUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public TargetedDamageIncreaseUpgrade(@NotNull Data data) {
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
                    if (!event.shooter().getUuid().equals(zombiesPlayer.getUUID()) ||
                        !(event.getEntity() instanceof Mob mob) ||
                        mob.data().tags().stream().noneMatch(data.tags::contains)) {
                        return;
                    }

                    event.setDamageAmount(Math.max(0, (float) (event.damageAmount() * data.damageMultiplier)));
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @DataObject
    public record Data(@NotNull Set<Key> tags,
        double damageMultiplier) {

    }
}
