package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.ethylene.mapper.annotation.Default;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.phantazm.commons.ExtensionHolder;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Model("mob.skill.group")
@Cache
public class GroupSkill implements SkillComponent {
    private static final Skill[] EMPTY_SKILL_ARRAY = new Skill[0];

    private final Data data;
    private final List<SkillComponent> delegates;

    @FactoryMethod
    public GroupSkill(@NotNull Data data, @NotNull @Child("delegates") List<SkillComponent> delegates) {
        this.data = Objects.requireNonNull(data);
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public @NotNull Skill apply(@NotNull ExtensionHolder holder) {
        Skill[] delegateSkills = new Skill[delegates.size()];
        for (int i = 0; i < delegateSkills.length; i++) {
            delegateSkills[i] = delegates.get(i).apply(holder);
        }

        List<Skill> tickables = new ArrayList<>(delegates.size());
        for (Skill skill : delegateSkills) {
            if (skill.needsTicking()) {
                tickables.add(skill);
            }
        }

        return new Internal(data.trigger, delegateSkills,
            tickables.isEmpty() ? EMPTY_SKILL_ARRAY : tickables.toArray(Skill[]::new));
    }

    @Default("""
        {
          trigger=null
        }
        """)
    @DataObject
    public record Data(@Nullable Trigger trigger,
        @NotNull @ChildPath("delegates") List<String> delegates) {
    }

    private record Internal(Trigger trigger,
        Skill[] delegates,
        Skill[] tickables) implements Skill {
        @Override
        public void init(@NotNull Mob mob) {
            for (Skill skill : delegates) {
                skill.init(mob);
            }
        }

        @Override
        public void use(@NotNull Mob mob) {
            for (Skill skill : delegates) {
                skill.use(mob);
            }
        }

        @Override
        public void tick(@NotNull Mob mob) {
            for (Skill skill : tickables) {
                skill.tick(mob);
            }
        }

        @Override
        public boolean needsTicking() {
            return tickables.length > 0;
        }

        @Override
        public void end(@NotNull Mob mob) {
            for (Skill skill : delegates) {
                skill.end(mob);
            }
        }
    }
}
