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
    protected final Mob self;
    private final Selector selector;

    public TargetedSkill(@NotNull Mob self, @NotNull Selector selector) {
        this.self = Objects.requireNonNull(self);
        this.selector = Objects.requireNonNull(selector);
    }

    @Override
    public final void use() {
        useOnTarget(selector.select());
    }

    /**
     * Called with the return value of {@link Selector#select()} whenever this skill is used.
     *
     * @param target the target of this skill
     */
    protected abstract void useOnTarget(@NotNull Target target);
}
