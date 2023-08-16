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
import org.phantazm.commons.InjectionStore;
import org.phantazm.mob2.Keys;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

public class RadialDamageSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public RadialDamageSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(injectionStore.get(Keys.MOB_KEY), selector.apply(injectionStore), data);
    }

    @DataObject
    public record Data(@NotNull @ChildPath("selector") String selector,
                       float damage,
                       boolean bypassArmor,
                       double range) {
        @Default("bypassArmor")
        public static @NotNull ConfigElement defaultBypassArmor() {
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
            target.forType(LivingEntity.class, livingEntity -> {
                float damage = (float)calculateDamage(self.getDistance(livingEntity));

                livingEntity.getAcquirable().sync(entity -> {
                    LivingEntity targetEntity = (LivingEntity)entity;
                    targetEntity.damage(Damage.fromEntity(self, damage), data.bypassArmor);
                });
            });
        }

        private double calculateDamage(double distanceToEntity) {
            return ((data.damage * Math.sqrt(data.range)) / data.range) *
                    (Math.sqrt(Math.max(0, -distanceToEntity + data.range)));
        }
    }
}
