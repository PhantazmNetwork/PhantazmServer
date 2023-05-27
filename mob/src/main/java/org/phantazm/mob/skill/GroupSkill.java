package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.ArrayList;
import java.util.List;

@Description("""
        A meta skill that calls any number of delegates, in the order that they are declared.
        """)
@Model("mob.skill.group")
@Cache(false)
public class GroupSkill implements Skill {
    private final List<Skill> delegates;
    private final List<Skill> tickingDelegates;

    private final boolean needsTicking;

    @FactoryMethod
    public GroupSkill(@NotNull @Child("delegates") List<Skill> delegates) {
        this.delegates = delegates;

        List<Skill> tickingDelegates = new ArrayList<>(delegates.size());
        for (Skill skill : delegates) {
            if (skill.needsTicking()) {
                tickingDelegates.add(skill);
            }
        }

        this.tickingDelegates = List.copyOf(tickingDelegates);
        this.needsTicking = !tickingDelegates.isEmpty();
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        for (Skill skill : delegates) {
            skill.use(self);
        }
    }

    @Override
    public void tick(long time, @NotNull PhantazmMob self) {
        for (Skill skill : tickingDelegates) {
            skill.tick(time, self);
        }
    }

    @Override
    public boolean needsTicking() {
        return needsTicking;
    }

    @DataObject
    public record Data(@NotNull @Description("The delegates to call") @ChildPath("delegates") List<String> delegates) {
    }
}
