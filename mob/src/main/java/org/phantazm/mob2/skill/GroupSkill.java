package org.phantazm.mob2.skill;

import com.github.steanky.element.core.annotation.ChildPath;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.InjectionStore;

import java.util.ArrayList;
import java.util.List;

public class GroupSkill implements SkillComponent {
    private final List<SkillComponent> delegates;

    @FactoryMethod
    public GroupSkill(@NotNull List<SkillComponent> delegates) {
        this.delegates = List.copyOf(delegates);
    }

    @Override
    public @NotNull Skill apply(@NotNull InjectionStore injectionStore) {
        Skill[] delegateSkills = new Skill[delegates.size()];
        for (int i = 0; i < delegateSkills.length; i++) {
            delegateSkills[i] = delegates.get(i).apply(injectionStore);
        }

        List<Skill> tickables = new ArrayList<>(delegates.size());
        for (Skill skill : delegateSkills) {
            if (skill.needsTicking()) {
                tickables.add(skill);
            }
        }

        return new Internal(delegateSkills, tickables.isEmpty() ? null : tickables.toArray(Skill[]::new));
    }

    @DataObject
    public record Data(@NotNull @ChildPath("delegates") List<String> delegates) {
    }

    private record Internal(Skill[] delegates, Skill[] tickables) implements Skill {
        @Override
        public void init() {
            for (Skill skill : delegates) {
                skill.init();
            }
        }

        @Override
        public void use() {
            for (Skill skill : delegates) {
                skill.use();
            }
        }

        @Override
        public void tick() {
            if (tickables == null) {
                return;
            }

            for (Skill skill : tickables) {
                skill.tick();
            }
        }

        @Override
        public boolean needsTicking() {
            return tickables != null;
        }

        @Override
        public void end() {
            for (Skill skill : delegates) {
                skill.end();
            }
        }
    }
}
