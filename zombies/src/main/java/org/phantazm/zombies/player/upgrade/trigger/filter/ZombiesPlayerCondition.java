package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.ZombiesPlayerEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.condition.zombies_player")
@Cache
public class ZombiesPlayerCondition implements EventConditionComponent {
    @FactoryMethod
    public ZombiesPlayerCondition() {
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(zombiesPlayer);
    }

    private record Internal(ZombiesPlayer zombiesPlayer) implements EventCondition<ZombiesPlayerEvent> {
        @Override
        public @NotNull Class<ZombiesPlayerEvent> eventType() {
            return ZombiesPlayerEvent.class;
        }

        @Override
        public boolean filter(@NotNull ZombiesPlayerEvent event) {
            return event.zombiesPlayer() == zombiesPlayer;
        }
    }
}
