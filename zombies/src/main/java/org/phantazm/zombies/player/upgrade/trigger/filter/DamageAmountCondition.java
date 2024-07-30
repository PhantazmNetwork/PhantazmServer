package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.core.CompareCondition;
import org.phantazm.zombies.event.trait.DamageEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

@Model("zombies.upgrade.filter.condition.damage_amount")
@Cache
public class DamageAmountCondition implements EventConditionComponent {
    private final Data data;

    @FactoryMethod
    public DamageAmountCondition(@NotNull Data data) {
        this.data = data;
    }

    @Override
    public @NotNull EventCondition<?> apply(@NotNull InjectionStore injectionStore, @NotNull ZombiesPlayer zombiesPlayer) {
        return new Internal(data);
    }

    private record Internal(Data data) implements EventCondition<DamageEvent> {
        @Override
        public @NotNull Class<DamageEvent> eventType() {
            return DamageEvent.class;
        }

        @Override
        public boolean filter(@NotNull DamageEvent event) {
            return data.condition.compare(event.damage().getAmount(), data.amount);
        }
    }

    @DataObject
    public record Data(double amount,
        @NotNull CompareCondition condition) {

    }
}
