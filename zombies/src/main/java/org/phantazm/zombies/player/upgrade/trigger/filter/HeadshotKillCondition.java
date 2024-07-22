package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.Tags;
import org.phantazm.zombies.event.trait.DamageEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.condition.headshot_kill")
@Cache
public class HeadshotKillCondition implements EventConditionComponent {
    private static final EventCondition<?> INSTANCE = new Internal();

    @FactoryMethod
    public HeadshotKillCondition() {
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return INSTANCE;
    }

    private static final class Internal implements EventCondition<DamageEvent> {
        @Override
        public @NotNull Class<DamageEvent> eventType() {
            return DamageEvent.class;
        }

        @Override
        public boolean filter(@NotNull DamageEvent event) {
            return event.damage().getTag(Tags.HEADSHOT_TAG);
        }
    }
}
