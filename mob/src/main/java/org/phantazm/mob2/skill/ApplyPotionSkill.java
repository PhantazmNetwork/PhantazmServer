package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.Trigger;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

import java.util.Objects;

@Model("mob.skill.potion")
@Cache
public class ApplyPotionSkill implements SkillComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public ApplyPotionSkill(@NotNull Data data, @NotNull @Child("selector") SelectorComponent selector) {
        this.data = Objects.requireNonNull(data);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        return new Internal(selector.apply(holder), data);
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(
        @Nullable Trigger trigger,
        @NotNull @ChildPath("selector") String selector,
        @NotNull Potion potion) {
    }

    private static class Internal extends TargetedSkill {
        private final Data data;

        private Internal(Selector selector, Data data) {
            super(selector);
            this.data = data;
        }

        @Override
        protected void useOnTarget(@NotNull Target target, @NotNull Mob mob) {
            target.forType(Entity.class, entity -> entity.addEffect(data.potion));
        }

        @Override
        public @Nullable Trigger trigger() {
            return data.trigger;
        }
    }
}
