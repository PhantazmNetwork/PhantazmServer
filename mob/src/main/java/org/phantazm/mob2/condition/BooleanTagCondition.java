package org.phantazm.mob2.condition;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import net.minestom.server.entity.Entity;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.selector.Selector;
import org.phantazm.mob2.selector.SelectorComponent;

@Model("mob.skill.condition.boolean_tag")
@Cache
public class BooleanTagCondition implements SkillConditionComponent {
    private final Data data;
    private final Tag<Boolean> tag;
    private final SelectorComponent selector;

    @FactoryMethod
    public BooleanTagCondition(@NotNull Data data,
        @NotNull @Child("selector") SelectorComponent selector) {
        this.data = data;
        this.tag = Tag.Boolean(data.tag).defaultValue(false);
        this.selector = selector;
    }

    @Override
    public @NotNull SkillCondition get() {
        return new Internal(tag, data.blacklist, selector.get());
    }

    @Default("""
        {
          selector={type='mob.selector.self'},
          blacklist=false
        }
        """)
    @DataObject
    public record Data(@NotNull String tag,
        boolean blacklist) {
    }

    private record Internal(Tag<Boolean> tag,
        boolean blacklist,
        Selector selector) implements SkillCondition {

        @Override
        public boolean test(@NotNull Mob mob) {
            for (Entity target : selector.select(mob).targets()) {
                if (target.getTag(this.tag) == blacklist) return false;
            }

            return true;
        }
    }
}