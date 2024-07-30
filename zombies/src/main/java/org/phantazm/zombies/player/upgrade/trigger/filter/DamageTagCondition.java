package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.DamageEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.condition.damage_tag")
@Cache
public class DamageTagCondition implements EventConditionComponent {
    private final Data data;

    @FactoryMethod
    public DamageTagCondition(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(Tag.Boolean(data.tag));
    }

    private record Internal(Tag<Boolean> tag) implements EventCondition<DamageEvent> {
        @Override
        public @NotNull Class<DamageEvent> eventType() {
            return DamageEvent.class;
        }

        @Override
        public boolean filter(@NotNull DamageEvent event) {
            return event.damage().getTag(tag);
        }
    }

    @DataObject
    public record Data(@NotNull String tag) {

    }
}
