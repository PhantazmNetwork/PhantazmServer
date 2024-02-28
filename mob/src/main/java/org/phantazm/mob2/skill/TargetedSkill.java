package org.phantazm.mob2.skill;

import org.jetbrains.annotations.NotNull;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Target;
import org.phantazm.mob2.selector.Selector;

import java.util.Objects;

/**
 * An abstract {@link Skill} implementation that is used on a specific {@link Target}.
 */
public abstract class TargetedSkill implements Skill {
    private final Selector selector;

    public TargetedSkill(@NotNull Selector selector) {
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public final void use(@NotNull Mob mob) {
        useOnTarget(selector.select(mob), mob);
    }

    /**
     * Called with the return value of {@link Selector#select(Mob)} whenever this skill is used.
     *
     * @param target the target of this skill
     * @param mob    the mob that is using the skill
     */
    protected abstract void useOnTarget(@NotNull Target target, @NotNull Mob mob);
}
