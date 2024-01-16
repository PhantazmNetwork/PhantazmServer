package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;

@Model("mob.skill.own_health_conditional")
@Cache
public class OwnHealthConditionalSkill implements SkillComponent {
    private final Data data;
    private final SkillComponent delegate;

    @FactoryMethod
    public OwnHealthConditionalSkill(@NotNull Data data, @NotNull @Child("delegate") SkillComponent delegate) {
        this.data = data;
        this.delegate = delegate;
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, mob, delegate.apply(mob, injectionStore));
    }

    public enum TriggerCondition {
        LESS_THAN,
        EQUAL_TO,
        GREATER_THAN
    }

    public enum AmountType {
        FLAT,
        PERCENTAGE
    }

    @DataObject
    public record Data(@NotNull TriggerCondition triggerCondition,
        @NotNull AmountType amountType,
        double amount,
        boolean triggersAutomatically) {
        @Default("amountType")
        public static @NotNull ConfigElement defaultAmountType() {
            return ConfigPrimitive.of("PERCENTAGE");
        }

        @Default("triggersAutomatically")
        public static @NotNull ConfigElement defaultTriggersAutomatically() {
            return ConfigPrimitive.FALSE;
        }
    }

    private static class Internal implements Skill {
        private final Data data;
        private final Mob self;
        private final Skill delegate;

        private final boolean needsTicking;

        private Internal(Data data, Mob self, Skill delegate) {
            this.data = data;
            this.self = self;
            this.delegate = delegate;

            this.needsTicking = delegate.needsTicking();

            if (data.triggersAutomatically) {
                self.addHealthListener(this::onHealthChange);
            }
        }

        @Override
        public void init() {
            delegate.init();
        }

        @Override
        public void use() {
            if (canTrigger(self.getHealth())) {
                delegate.use();
            }
        }

        @Override
        public void end() {
            delegate.end();
        }

        @Override
        public boolean needsTicking() {
            return needsTicking;
        }

        @Override
        public void tick() {
            delegate.tick();
        }

        private boolean canTrigger(float health) {
            float actualHealth = adjust(health);
            return switch (data.triggerCondition) {
                case LESS_THAN -> actualHealth < data.amount;
                case EQUAL_TO -> actualHealth == data.amount;
                case GREATER_THAN -> actualHealth > data.amount;
            };
        }

        private float adjust(float health) {
            return switch (data.amountType) {
                case FLAT -> health;
                case PERCENTAGE -> health / self.getMaxHealth();
            };
        }

        private void onHealthChange(float newHealth) {
            if (canTrigger(newHealth)) {
                delegate.use();
            }
        }
    }
}
