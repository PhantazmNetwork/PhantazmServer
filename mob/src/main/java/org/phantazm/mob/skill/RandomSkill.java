package org.phantazm.mob.skill;

import com.github.steanky.element.core.annotation.*;
import com.github.steanky.element.core.annotation.document.Description;
import org.jetbrains.annotations.NotNull;
import org.phantazm.mob.PhantazmMob;

import java.util.Random;

@Description("""
        A meta skill that has a configurable random chance to trigger another meta skill. The chance is a decimal value
        ranging from 0 to 1. Values less than or equal 0 will never trigger; values greater than or equal to 1 will
        always trigger.
        """)
@Model("mob.skill.random")
@Cache(false)
public class RandomSkill implements Skill {
    private final Data data;
    private final Skill delegate;
    private final Random random;

    @FactoryMethod
    public RandomSkill(@NotNull Data data, @NotNull @Child("delegate") Skill delegate) {
        this.data = data;
        this.delegate = delegate;
        this.random = new Random();
    }

    @Override
    public void use(@NotNull PhantazmMob self) {
        if (data.chance <= 0) {
            return;
        }

        if (data.chance >= 1 || random.nextDouble() < data.chance) {
            delegate.use(self);
        }
    }

    @DataObject
    public record Data(@Description("Chance that this skill will call its delegate") double chance,
                       @Description("The delegate to call on a successful roll") @NotNull @ChildPath(
                               "delegate") String delegate) {

    }
}
