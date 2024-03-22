package org.phantazm.mob2.condition;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

@Model("mob.skill.condition.health")
@Cache
public class HealthCondition implements SkillConditionComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public HealthCondition(@NotNull Data data,
        @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull SkillCondition apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, selector.apply(holder));
    }

    public enum Condition {
        LESS_THAN,
        EQUAL_TO,
        GREATER_THAN
    }

    public enum AmountType {
        FLAT,
        PERCENTAGE
    }

    @Default("""
        {
          amountType='PERCENTAGE'
        }
        """)
    @DataObject
    public record Data(@NotNull HealthCondition.Condition condition,
        @NotNull AmountType amountType,
        double amount) {
    }

    private record Internal(Data data,
        Selector selector) implements SkillCondition {

        @Override
        public boolean test(@NotNull Mob mob) {
            return selector.select(mob).forType(LivingEntity.class).filter(this::canTrigger).isPresent();
        }

        private boolean canTrigger(LivingEntity target) {
            float actualHealth = adjust(target.getHealth(), target);
            return switch (data.condition) {
                case LESS_THAN -> actualHealth < data.amount;
                case EQUAL_TO -> actualHealth == data.amount;
                case GREATER_THAN -> actualHealth > data.amount;
            };
        }

        private float adjust(float health, LivingEntity target) {
            return switch (data.amountType) {
                case FLAT -> health;
                case PERCENTAGE -> health / target.getMaxHealth();
            };
        }
    }
}