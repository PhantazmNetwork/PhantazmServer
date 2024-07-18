package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.attribute.AttributeModifier;
import net.minestom.server.attribute.AttributeOperation;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.Attributes;
import org.phantazm.zombies.event.player.ZombiesPlayerDamageEvent;
import org.phantazm.zombies.event.player.ZombiesPlayerKillMobEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Model("zombies.upgrade.resistance_points")
@Cache
public class ResistancePointsUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public ResistancePointsUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private static final UUID id = UUID.randomUUID();
                private static final String idString = id.toString();

                private final EventListener<ZombiesPlayerKillMobEvent> killEvent = EventListener
                    .builder(ZombiesPlayerKillMobEvent.class).handler(this::handleMobDeath).build();
                private final EventListener<ZombiesPlayerDamageEvent> damageEvent = EventListener
                    .builder(ZombiesPlayerDamageEvent.class).handler(this::handleDamage).build();

                private final AtomicInteger resistanceLevel = new AtomicInteger();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(killEvent);
                    zombiesPlayer.getScene().sceneNode().addListener(damageEvent);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(killEvent);
                    zombiesPlayer.getScene().sceneNode().removeListener(damageEvent);
                }

                private void handleMobDeath(ZombiesPlayerKillMobEvent event) {
                    if (event.getZombiesPlayer() != zombiesPlayer) return;
                    applyLevel(event.getPlayer(), resistanceLevel.updateAndGet(current ->
                        Math.min(current + 1, data.maxPoints)));
                }

                private void handleDamage(ZombiesPlayerDamageEvent event) {
                    if (event.getZombiesPlayer() != zombiesPlayer) return;
                    applyLevel(event.getPlayer(), resistanceLevel.updateAndGet(current -> Math.max(current - 1, 0)));
                }

                private void applyLevel(Player player, int level) {
                    player.getAttribute(Attributes.get(data.attribute)).removeModifier(id);
                    if (level == 0) return;

                    player.getAttribute(Attributes.get(data.attribute)).addModifier(new AttributeModifier(id, idString,
                        data.baseValue * level, data.operation));
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @DataObject
    public record Data(int maxPoints,
        @NotNull String attribute,
        double baseValue,
        @NotNull AttributeOperation operation) {

    }
}
