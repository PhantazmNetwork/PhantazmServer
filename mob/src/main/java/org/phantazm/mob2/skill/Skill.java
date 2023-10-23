package org.phantazm.mob2.skill;

import org.jetbrains.annotations.Nullable;
import org.phantazm.mob2.Mob;
import org.phantazm.mob2.Trigger;

import java.util.Collection;

/**
 * Represents a special behavior for a mob. This behavior can be triggered by certain conditions, execute periodically,
 * delegate to other skills, have defined starting and ending states, or some combination of these qualities.
 * <p>
 * A {@link Mob} may manage many instances of {@link Skill}, but for each skill instance, there may only be one
 * associated mob.
 * <p>
 * Some methods of this interface must support concurrent access, as well as access off of the thread that is ticking
 * the entity.
 */
public interface Skill {
    /**
     * The condition that will trigger the {@link Skill#use()} method to be called. If {@code null}, this method will
     * never be called by the mob itself, but may still be called if this skill is a <i>delegate</i> of another skill.
     * <p>
     * <b>Thread Behavior</b>: This method safely supports being called by threads other than the owning entity's
     * current tick thread. It also supports concurrent access.
     *
     * @return the {@link Trigger} used by this skill
     */
    default @Nullable Trigger trigger() {
        return null;
    }

    /**
     * Initializes this skill, performing any necessary setup operations. Called once, when the skill is first added to
     * a mob, in the {@link Mob#addSkill(Skill)} or {@link Mob#addSkills(Collection)} methods.
     * <p>
     * Generally speaking, it is not appropriate to call this method manually. Implementations <b>might not</b> guard
     * against state corruption caused by multiple invocations.
     * <p>
     * When this method is called, the entity that owns this skill <b>may or may not</b> have an instance set.
     * <p>
     * <b>Thread Behavior</b>: This method safely supports being called by threads other than the owning entity's
     * current tick thread. It does <b>not</b> support concurrent access.
     */
    default void init() {
    }

    /**
     * Executes the action specified by this skill. This may be called internally, from within the owning {@link Mob}
     * class, or externally by other code to trigger the skill "manually".
     * <p>
     * <b>Thread Behavior</b>: This method safely supports being called by threads other than the owning entity's
     * current tick thread. It also supports concurrent access.
     */
    default void use() {
    }

    /**
     * Ticks this skill, enabling periodic behavior. Assuming no server lag or other conditions, this method should be
     * called approximately 20 times per second.
     * <p>
     * If the {@link Skill#needsTicking()} method returns false, it is not <b>necessary</b> to call this method in order
     * for the behavior of this skill to be correct; however, users <b>must</b> be able to do so without side effects.
     * Conversely, if {@code needsTicking} returns {@code true}, this method {@code will} be called by the owning
     * entity.
     * <p>
     * <b>Thread Behavior</b>: It is not safe to call this method by any thread other than the owning's entity's
     * current tick thread, unless proper synchronization is performed. It does not support concurrent access.
     */
    default void tick() {
    }

    /**
     * Reports whether this skill requires ticking or not. Defaults to {@code false}. The return value of this method
     * <b>must not</b> change for the lifetime of the skill object; if it does, users <b>need not</b> respect the new
     * ticking preference.
     * <p>
     * <b>Thread Behavior</b>: This method safely supports being called by threads other than the owning entity's
     * current tick thread. It also supports concurrent access.
     *
     * @return {@code true} if this skill needs to be ticked; false otherwise
     */
    default boolean needsTicking() {
        return false;
    }

    /**
     * Ends this skill, performing any necessary cleanup actions. This method will be called if a skill is removed from
     * an entity, using the {@link Mob#removeSkill(Skill)} method, or if the mob is removed from the instance using
     * {@link Mob#remove()}.
     * <p>
     * <b>Thread Behavior</b>: This method safely supports being called by threads other than the owning entity's
     * current tick thread. It does not support concurrent access.
     */
    default void end() {
    }
}
