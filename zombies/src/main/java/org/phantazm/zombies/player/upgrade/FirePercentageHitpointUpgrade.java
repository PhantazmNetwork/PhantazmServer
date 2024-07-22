package org.phantazm.zombies.player.upgrade;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.event.EventListener;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.tick.Activable;
import org.phantazm.zombies.event.player.ZombiesPlayerProcFireEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.fire_percentage_hitpoint")
@Cache
public class FirePercentageHitpointUpgrade implements PlayerUpgradeComponent {
    private final Data data;

    @FactoryMethod
    public FirePercentageHitpointUpgrade(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull PlayerUpgrade apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer player) {
        return new Internal(player, data);
    }

    private static class Internal extends GuardedPlayerUpgrade {
        private Internal(ZombiesPlayer zombiesPlayer, Data data) {
            super(Activable.threadsafeWrapper(new Activable() {
                private final EventListener<ZombiesPlayerProcFireEvent> event = EventListener.builder(ZombiesPlayerProcFireEvent.class)
                    .handler(this::handleFireProc).build();

                @Override
                public void start() {
                    zombiesPlayer.getScene().sceneNode().addListener(event);
                }

                @Override
                public void end() {
                    zombiesPlayer.getScene().sceneNode().removeListener(event);
                }

                private void handleFireProc(ZombiesPlayerProcFireEvent event) {
                    if (event.zombiesPlayer() != zombiesPlayer) {
                        return;
                    }

                    event.setDamageAmount((float) Math.max((event.target().getHealth() * data.hitpointFactor), event.damageAmount()));
                }
            }), zombiesPlayer);
        }

        @Override
        public boolean needsTicking() {
            return false;
        }
    }

    @DataObject
    public record Data(double hitpointFactor) {

    }
}
