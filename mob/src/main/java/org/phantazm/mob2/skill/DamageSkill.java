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
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data);
    }

    @Default("""
        {
          trigger=null,
          knockback=0,
          horizontal=true,
          bypassArmor=false
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        float amount,
        float knockback,
        boolean horizontal,
        boolean bypassArmor) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Selector selector, Data data) {
            super(selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            target.forType(LivingEntity.class, livingEntity -> livingEntity.getAcquirable().sync(e -> {
                LivingEntity entity = (LivingEntity) e;
                if (data.knockback > 0) {
                    double angle = mob.getPosition().yaw() * (Math.PI / 180);
                    entity.takeKnockback(data.knockback, data.horizontal, Math.sin(angle), -Math.cos(angle));
                }

                entity.damage(Damage.fromEntity(mob, data.amount), data.bypassArmor);
            }));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
