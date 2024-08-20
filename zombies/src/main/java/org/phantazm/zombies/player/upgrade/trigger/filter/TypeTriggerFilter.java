package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.player.upgrade.PlayerUpgrade;
import org.phantazm.zombies.player.upgrade.trigger.TriggerData;

import java.util.ArrayList;
import java.util.List;

@Model("zombies.upgrade.filter.type")
@Cache
public class TypeTriggerFilter implements TriggerFilterComponent {
    private final Data data;
    private final List<EventConditionComponent> conditions;

    @FactoryMethod
    public TypeTriggerFilter(@NotNull Data data, @Child("conditions") List<EventConditionComponent> conditions) {
        this.data = data;
        this.conditions = conditions;
    }

    @Override
    public @NotNull TriggerFilter apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        List<EventCondition<?>> conditions = new ArrayList<>(this.conditions.size());
        for (EventConditionComponent component : this.conditions) {
            conditions.add(component.apply(injectionStore, zombiesPlayer));
        }

        return new Internal(data, conditions);
    }

    private record Internal(Data data,
        List<EventCondition<?>> conditions) implements TriggerFilter {

        private <T extends Event> boolean test(EventCondition<T> condition, Event event) {
            return condition.eventType().isAssignableFrom(event.getClass()) &&
                condition.filter(condition.eventType().cast(event));
        }

        @Override
        public boolean test(@NotNull PlayerUpgrade upgrade, @NotNull ZombiesPlayer zombiesPlayer, @NotNull TriggerData triggerData) {
            if (!(triggerData.raw() instanceof Event event)) {
                return false;
            }

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
