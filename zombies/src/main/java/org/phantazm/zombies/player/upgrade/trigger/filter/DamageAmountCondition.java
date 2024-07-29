package org.phantazm.zombies.player.upgrade.trigger.filter;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
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
            return switch (data.condition) {
                case GREATER -> event.damage().getAmount() > data.amount;
                case LESS_THAN -> event.damage().getAmount() < data.amount;
                case GREATER_OR_EQUAL -> event.damage().getAmount() >= data.amount;
                case LESS_THAN_OR_EQUAL -> event.damage().getAmount() <= data.amount;
                case EQUAL -> event.damage().getAmount() == data.amount;
            };
        }
    }

    public enum Condition {
        GREATER,
        LESS_THAN,
        GREATER_OR_EQUAL,
        LESS_THAN_OR_EQUAL,
        EQUAL
    }

    @DataObject
    public record Data(double amount,
        @NotNull Condition condition) {

    }
}
