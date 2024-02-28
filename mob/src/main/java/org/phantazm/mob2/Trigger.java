package org.phantazm.mob2;

import org.phantazm.mob2.skill.Skill;

/**
 * Represents a predefined set of <i>triggers</i>. Triggers are specific conditions that result in the
 * {@link Skill#use(Mob)} method being invoked for any skills dependent on the trigger.
 */
public enum Trigger {
    /**
     * Trigger that activates whenever the entity is updated, or 20 times per second.
     */
    TICK,

    /**
     * Trigger that activates whenever an entity dies, but before it is removed from the world.
     */
    DEATH,

    /**
     * Trigger that activates when an entity spawns (has its instance set).
     */
    SPAWN,

    /**
     * Trigger that activates whenever an entity attacks another entity.
     */
    ATTACK,

    /**
     * Trigger that activates when a player interacts with (right-clicks) this entity.
     */
    INTERACT,

    /**
     * Trigger that activates whenever this entity is damaged.
     */
    DAMAGED
}
