package org.phantazm.mob2.condition;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
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
        return new Internal(data.flag, data.blacklist, selector.get());
    }

    @Default("""
        {
          selector={type='mob.selector.self'},
          blacklist=false
        }
        """)
    @DataObject
    public record Data(@NotNull Key flag,
        boolean blacklist) {
    }

    private record Internal(Key flag,
        boolean blacklist,
        Selector selector) implements SkillCondition {

        @Override
        public boolean test(@NotNull Mob mob) {
            for (Entity target : selector.select(mob).targets()) {
                if (!(target instanceof Mob targetMob)) continue;
                if (targetMob.data().tags().contains(flag) != blacklist) return false;
            }

            return true;
        }
    }
}