package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

@Model("mob.skill.damage_over_time")
@Cache
public class DamageOverTimeSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;
    private final ValidatorComponent validator;

    @FactoryMethod
    public DamageOverTimeSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
        @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
        this.validator = Objects.requireNonNull(validator);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, mob, selector.apply(mob, injectionStore), validator.apply(mob, injectionStore));
    }

    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        @NotNull @ChildPath("validator") String validator,
        int damageInterval,
        int damageTime,
        float damageAmount,
        boolean bypassArmor) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

        @Default("bypassArmor")
        public static @NotNull ConfigElement defaultBypassArmor() {
            return ConfigPrimitive.of(false);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final Deque<Entry> targets;
        private final Validator validator;

        private int ticks;

        private Internal(Data data, Mob self, Selector selector, Validator validator) {
            super(self, selector);
            this.data = data;
            this.targets = new ArrayDeque<>();
            this.validator = validator;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            self.getAcquirable().sync(ignored -> {
                target.forType(LivingEntity.class, livingEntity -> {
                    if (!validator.valid(livingEntity)) {
                        return;
                    }

                    addDamageTarget(livingEntity, ticks);
                });
            });
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void tick() {
            Iterator<Entry> iterator = targets.iterator();
            while (iterator.hasNext()) {
                Entry entry = iterator.next();
                if (entry.target.isDead() || entry.target.isRemoved() || !validator.valid(entry.target)) {
                    iterator.remove();
                    continue;
                }

                int sinceStart = ticks - entry.start;
                if (sinceStart % data.damageInterval == 0) {
                    damageTarget(entry.target);
                }

                if (sinceStart >= data.damageTime) {
                    iterator.remove();
                }
            }

            ticks++;
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        private void damageTarget(LivingEntity target) {
            target.getAcquirable().sync(targetEntity -> {
                ((LivingEntity) targetEntity).damage(Damage.fromEntity(self, data.damageAmount), data.bypassArmor);
            });
        }

        private void addDamageTarget(LivingEntity livingEntity, int ticks) {
            targets.add(new Entry(livingEntity, ticks));
        }

        private record Entry(LivingEntity target,
            int start) {
        }
    }
}
