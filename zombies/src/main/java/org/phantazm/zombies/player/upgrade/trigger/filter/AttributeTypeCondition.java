package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.event.trait.AttributeEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Set;

@Model("zombies.upgrade.filter.condition.attribute_type")
@Cache
public class AttributeTypeCondition implements EventConditionComponent {
    private final Data data;

    @FactoryMethod
    public AttributeTypeCondition(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private static final class Internal implements EventCondition<AttributeEvent> {
        private final Data data;

        private Internal(Data data) {
            this.data = data;
        }

        @Override
        public @NotNull Class<AttributeEvent> eventType() {
            return AttributeEvent.class;
        }

        @Override
        public boolean filter(@NotNull AttributeEvent event) {
            return data.attributes.contains(event.attribute().key()) != data.blacklist;
        }
    }

    @DataObject
    public record Data(@NotNull Set<String> attributes,
        boolean blacklist) {
    }
}
