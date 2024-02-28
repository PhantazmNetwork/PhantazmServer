package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

@Model("mob.skill.radial_damage")
@Cache
public class RadialDamageSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public RadialDamageSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data);
    }

    @Default("""
        {
          trigger=null,
          bypassArmor=false
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        float damage,
        boolean bypassArmor,
        double range) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Selector selector, Data data) {
            super(selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            target.forType(LivingEntity.class, livingEntity -> {
                float damage = (float) calculateDamage(mob.getDistance(livingEntity));

                livingEntity.scheduleNextTick(self -> {
                    ((LivingEntity) self).damage(Damage.fromEntity(mob, damage), data.bypassArmor);
                });
            });
        }

        private double calculateDamage(double distanceToEntity) {
            return ((data.damage * Math.sqrt(data.range)) / data.range) *
                (Math.sqrt(Math.max(0, -distanceToEntity + data.range)));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
