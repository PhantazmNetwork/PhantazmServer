package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
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
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.BasicMobSpawner;
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

    private static class Extension {
        private int ticks;
        private Task tickTask;
        private final Deque<Internal.Entry> targets = new ArrayDeque<>();
    }

    @FactoryMethod
    public DamageOverTimeSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector,
        @NotNull @Child("validator") ValidatorComponent validator) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
        this.validator = Objects.requireNonNull(validator);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(data, holder.requestKey(Extension.class), selector.apply(holder),
            validator.apply(holder));
    }

    @Default("""
        {
          sound=null,
          trigger=null,
          bypassArmor=false,
          exceedMobLifetime=true
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @Nullable Sound sound,
        int damageInterval,
        int damageTime,
        float damageAmount,
        boolean bypassArmor,
        boolean exceedMobLifetime) {

    }

    private static class Internal extends TargetedSkill {
        private final Data data;
        private final ExtensionHolder.Key<Extension> key;
        private final Validator validator;

        private Internal(Data data, ExtensionHolder.Key<Extension> key, Selector selector, Validator validator) {
            super(selector);
            this.data = data;
            this.key = key;
            this.validator = validator;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);
            mob.getAcquirable().sync(ignored -> {
                target.forType(LivingEntity.class, livingEntity -> {
                    addDamageTarget(livingEntity, ext);
                });
            });
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }

        @Override
        public void init(@NotNull Mob mob) {
            mob.extensions().set(key, new Extension());
        }

        @Override
        public void tick(@NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);
            Task tickTask = ext.tickTask;

            if (ext.targets.isEmpty() && tickTask != null) {
                tickTask.cancel();
                ext.tickTask = null;
                return;
            }

            Iterator<Entry> iterator = ext.targets.iterator();
            while (iterator.hasNext()) {
                Entry entry = iterator.next();
                LivingEntity target = entry.target.get();

                if (target == null || target.isDead() || target.isRemoved() || !validator.valid(mob, target)) {
                    iterator.remove();
                    continue;
                }

                int sinceStart = ext.ticks - entry.start;
                if (sinceStart % data.damageInterval == 0) {
                    damageTarget(mob, target);
                }

                if (sinceStart >= data.damageTime) {
                    iterator.remove();
                }
            }

            ext.ticks++;
        }

        @Override
        public void end(@NotNull Mob mob) {
            Extension ext = mob.extensions().get(key);
            if (!data.exceedMobLifetime) {
                return;
            }

            Scheduler scheduler = mob.extensions().getOrDefault(BasicMobSpawner.SCHEDULER_KEY,
                MinecraftServer::getSchedulerManager);
            mob.getAcquirable().sync(self -> {
                if (ext.targets.isEmpty() || ext.tickTask != null) {
                    return;
                }

                ext.tickTask = scheduler.scheduleTask(() -> tick(mob), TaskSchedule.nextTick(), TaskSchedule.nextTick());
            });
        }

        @Override
        public boolean needsTicking() {
            return true;
        }

        private void damageTarget(Mob self, LivingEntity target) {
            target.getAcquirable().sync(targetEntity -> {
                if (data.sound != null && targetEntity instanceof Player player) {
                    player.playSound(data.sound);
                }
                ((LivingEntity) targetEntity).damage(Damage.fromEntity(self, data.damageAmount), data.bypassArmor);
            });
        }

        private void addDamageTarget(LivingEntity livingEntity, Extension ext) {
            ext.targets.add(new Entry(new WeakReference<>(livingEntity), ext.ticks));
        }

        private record Entry(Reference<LivingEntity> target,
            int start) {
        }
    }
}
