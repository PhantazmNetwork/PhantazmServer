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

import java.util.Objects;

@Model("mob.skill.damage")
@Cache
public class DamageSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public DamageSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull Mob mob, @NotNull InjectionStore injectionStore) {
        return new Internal(mob, selector.apply(mob, injectionStore), data);
    }

    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        float amount,
        float knockback,
        boolean horizontal,
        boolean armorBypassing) {
        @Default("trigger")
        public static @NotNull ConfigElement defaultTrigger() {
            return ConfigPrimitive.NULL;
        }

        @Default("knockback")
        public static @NotNull ConfigElement defaultKnockback() {
            return ConfigPrimitive.of(0);
        }

        @Default("horizontal")
        public static @NotNull ConfigElement defaultHorizontal() {
            return ConfigPrimitive.of(true);
        }

        @Default("armorBypassing")
        public static @NotNull ConfigElement defaultArmorBypassing() {
            return ConfigPrimitive.of(false);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Mob self, Selector selector, Data data) {
            super(self, selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            target.forType(LivingEntity.class, livingEntity -> livingEntity.getAcquirable().sync(e -> {
                LivingEntity entity = (LivingEntity) e;
                if (data.knockback > 0) {
                    double angle = self.getPosition().withLookAt(entity.getPosition()).yaw() * (Math.PI / 180);
                    entity.takeKnockback(data.knockback, data.horizontal, Math.sin(angle), -Math.cos(angle));
                }

                entity.damage(Damage.fromEntity(self, data.amount), data.armorBypassing);
            }));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
