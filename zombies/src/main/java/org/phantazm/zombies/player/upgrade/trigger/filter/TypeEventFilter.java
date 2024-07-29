package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.ArrayList;
import java.util.List;

@Model("zombies.upgrade.filter.type")
@Cache
public class TypeEventFilter implements EventFilterComponent {
    private final Data data;
    private final List<EventConditionComponent> conditions;

    @FactoryMethod
    public TypeEventFilter(@NotNull Data data, @Child("conditions") List<EventConditionComponent> conditions) {
        this.data = data;
        this.conditions = conditions;
    }

    @Override
    public @NotNull EventFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        List<EventCondition<?>> conditions = new ArrayList<>(this.conditions.size());
        for (EventConditionComponent component : this.conditions) {
            conditions.add(component.apply(injectionStore, zombiesPlayer));
        }

        return new Internal(data, conditions);
    }

    private static final class Internal implements EventFilter {
        private final Data data;
        private final List<EventCondition<?>> conditions;

        private Internal(Data data, List<EventCondition<?>> conditions) {
            this.data = data;
            this.conditions = conditions;
        }

        @Override
        public boolean test(Event event) {
            return switch (data.evaluation) {
                case AND -> {
                    for (EventCondition<?> condition : conditions) {
                        if (!test(condition, event)) yield false;
                    }

                    yield true;
                }
                case OR -> {
                    for (EventCondition<?> condition : conditions) {
                        if (test(condition, event)) yield true;
                    }

                    yield conditions.isEmpty();
                }
            };
        }

        private <T extends Event> boolean test(EventCondition<T> condition, Event event) {
            return condition.eventType().isAssignableFrom(event.getClass()) &&
                condition.filter(condition.eventType().cast(event));
        }
    }

    public enum Evaluation {
        AND,
        OR
    }

    @Default("""
        {
          evaluation='AND',
          conditions=[]
        }
        """)
    @DataObject
    public record Data(@NotNull Evaluation evaluation) {

    }
}
