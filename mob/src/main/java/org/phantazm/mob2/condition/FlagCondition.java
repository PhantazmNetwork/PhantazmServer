package org.phantazm.mob2.condition;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

@Model("mob.skill.condition.flag")
@Cache
public class FlagCondition implements SkillConditionComponent {
    private final Data data;
    private final SelectorComponent selector;

    @FactoryMethod
    public FlagCondition(@NotNull Data data,
        @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.selector = selector;
    }

    @Override
    public @NotNull SkillCondition get() {
        return new Internal(data, selector.get());
    }

    @Default("""
        {
          selector='mob.selector.self',
          blacklist=false
        }
        """)
    @DataObject
    public record Data(@NotNull Key flag,
        boolean blacklist) {
    }

    private record Internal(Data data,
        Selector selector) implements SkillCondition {

        @Override
        public boolean test(@NotNull Mob mob) {
            return selector.select(mob).forType(Mob.class)
                .map(sample -> (sample.data().tags().contains(data.flag) != data.blacklist))
                .orElse(false);
        }
    }
}