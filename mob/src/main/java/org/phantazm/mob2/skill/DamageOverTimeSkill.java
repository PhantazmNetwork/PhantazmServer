package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
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
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;
import org.phantazm.mob2.Trigger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

public class DamageOverTimeSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public DamageOverTimeSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(data, mob, selector.apply(mob, injectionStore));
    }

    @DataObject
    public record Data(@Nullable Trigger trigger,
                       @NotNull @ChildPath("selector") String selector,
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

        private int ticks;

        private Internal(Data data, Mob self, Selector selector) {
            super(self, selector);
            this.data = data;
            this.targets = new ArrayDeque<>();
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            self.getAcquirable().sync(ignored -> {
                target.forType(LivingEntity.class, livingEntity -> addDamageTarget(livingEntity, ticks));
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
                if (entry.target.isDead() || entry.target.isRemoved()) {
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
                LivingEntity livingEntity = (LivingEntity)targetEntity;
                livingEntity.damage(Damage.fromEntity(self, data.damageAmount), data.bypassArmor);
            });
        }

        private void addDamageTarget(LivingEntity livingEntity, int ticks) {
            targets.add(new Entry(livingEntity, ticks));
        }

        private record Entry(LivingEntity target, int start) {
        }
    }
}
