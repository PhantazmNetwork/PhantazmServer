package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.InjectionKeys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;
import org.phantazm.mob2.validator.Validator;
import org.phantazm.mob2.validator.ValidatorComponent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
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
        Scheduler scheduler = injectionStore.getOrDefault(InjectionKeys.SCHEDULER,
            MinecraftServer::getSchedulerManager);
        return new Internal(data, mob, selector.apply(mob, injectionStore), validator.apply(mob, injectionStore),
            scheduler);
    }

    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        @NotNull @ChildPath("validator") String validator,
        @Nullable Sound sound,
        int damageInterval,
        int damageTime,
        float damageAmount,
        boolean bypassArmor,
        boolean exceedMobLifetime) {
        @Default("sound")
        public static @NotNull ConfigElement defaultSound() {
            return ConfigPrimitive.NULL;
        }

        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

        @Default("bypassArmor")
        public static @NotNull ConfigElement defaultBypassArmor() {
            return ConfigPrimitive.of(false);
        }

        @Default("exceedMobLifetime")
        public static @NotNull ConfigElement defaultExceedMobLifetime() {
            return ConfigPrimitive.of(true);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final Deque<Entry> targets;
        private final Validator validator;
        private final Scheduler scheduler;

        private int ticks;
        private Task tickTask;

        private Internal(Data data, Mob self, Selector selector, Validator validator, Scheduler scheduler) {
            super(self, selector);
            this.data = data;
            this.targets = new ArrayDeque<>();
            this.validator = validator;
            this.scheduler = scheduler;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            self.getAcquirable().sync(ignored -> {
                target.forType(LivingEntity.class, livingEntity -> {
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
            Task tickTask = this.tickTask;
            if (targets.isEmpty() && tickTask != null) {
                tickTask.cancel();
                this.tickTask = null;
                return;
            }

            Iterator<Entry> iterator = targets.iterator();
            while (iterator.hasNext()) {
                Entry entry = iterator.next();
                LivingEntity target = entry.target.get();

                if (target == null || target.isDead() || target.isRemoved() || !validator.valid(target)) {
                    iterator.remove();
                    continue;
                }

                int sinceStart = ticks - entry.start;
                if (sinceStart % data.damageInterval == 0) {
                    damageTarget(target);
                }

                if (sinceStart >= data.damageTime) {
                    iterator.remove();
                }
            }

            ticks++;
        }

        @Override
        public void end() {
            if (!data.exceedMobLifetime) {
                return;
            }

            self.getAcquirable().sync(self -> {
                if (targets.isEmpty() || tickTask != null) {
                    return;
                }

                tickTask = scheduler.scheduleTask(this::tick, TaskSchedule.nextTick(), TaskSchedule.nextTick());
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        private void damageTarget(LivingEntity target) {
            target.getAcquirable().sync(targetEntity -> {
                if (data.sound != null && targetEntity instanceof Player player) {
                    player.playSound(data.sound);
                }
                ((LivingEntity) targetEntity).damage(Damage.fromEntity(self, data.damageAmount), data.bypassArmor);
            });
        }

        private void addDamageTarget(LivingEntity livingEntity, int ticks) {
            targets.add(new Entry(new WeakReference<>(livingEntity), ticks));
        }

        private record Entry(Reference<LivingEntity> target,
            int start) {
        }
    }
}
