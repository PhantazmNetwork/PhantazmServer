package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.GunEvent;
import org.phantazm.zombies.event.trait.ShooterEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.condition.player_shooter")
@Cache
public class PlayerShooterCondition implements EventConditionComponent {
    @FactoryMethod
    public PlayerShooterCondition() {
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer);
    }

    private record Internal(ZombiesPlayer zombiesPlayer) implements EventCondition<GunEvent> {
        @Override
        public @NotNull Class<GunEvent> eventType() {
            return GunEvent.class;
        }

        @Override
        public boolean filter(@NotNull GunEvent event) {
            if (event instanceof ShooterEvent shooterEvent) {
                return shooterEvent.shooter().getUuid().equals(zombiesPlayer.getUUID());
            }

            return event.gun().owner().map(entity -> entity.getUuid().equals(zombiesPlayer.getUUID()))
                .orElse(false);
        }
    }
}
