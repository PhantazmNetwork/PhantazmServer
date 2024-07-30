package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
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

    private record Internal(ZombiesPlayer zombiesPlayer) implements EventCondition<ShooterEvent> {
        @Override
        public @NotNull Class<ShooterEvent> eventType() {
            return ShooterEvent.class;
        }

        @Override
        public boolean filter(@NotNull ShooterEvent event) {
            return event.shooter().getUuid().equals(zombiesPlayer.getUUID());
        }
    }
}
