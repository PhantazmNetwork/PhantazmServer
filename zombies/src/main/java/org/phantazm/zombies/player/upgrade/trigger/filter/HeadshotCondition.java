package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.equipment.EntityDamageByGunEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.condition.headshot")
@Cache
public class HeadshotCondition implements EventConditionComponent {
    private static final EventCondition<?> INSTANCE = new Internal();

    @FactoryMethod
    public HeadshotCondition() {
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return INSTANCE;
    }

    private record Internal() implements EventCondition<EntityDamageByGunEvent> {
        @Override
        public @NotNull Class<EntityDamageByGunEvent> eventType() {
            return EntityDamageByGunEvent.class;
        }

        @Override
        public boolean filter(@NotNull EntityDamageByGunEvent event) {
            return event.isHeadshot();
        }
    }
}
