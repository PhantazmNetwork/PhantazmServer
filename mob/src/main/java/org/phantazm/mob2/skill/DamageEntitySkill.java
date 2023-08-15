package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.Child;
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

public class DamageEntitySkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public DamageEntitySkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        return new Internal(injectionStore.get(Keys.MOB_KEY), selector.apply(injectionStore), data);
    }

    @DataObject
    public record Data(float amount, boolean armorBypassing) {
        @Default("armorBypassing")
        public static @NotNull ConfigElement defaultArmorBypassing() {
            return ConfigPrimitive.of(false);
        }
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(@NotNull Mob self, @NotNull Selector selector, @NotNull Data data) {
            super(self, selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target) {
            target.typedTargets(LivingEntity.class,
                    livingEntity -> livingEntity.damage(Damage.fromEntity(self, data.amount), data.armorBypassing));
        }
    }
}
