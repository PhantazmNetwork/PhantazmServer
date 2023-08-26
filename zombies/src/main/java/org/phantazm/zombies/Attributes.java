package org.phantazm.zombies;

import net.minestom.server.attribute.Attribute;

public final class Attributes {
    /**
     * Expansion applied to the hitbox of entities. Used to make things easier (or harder) to hit with bullets.
     */
    public static final Attribute HITBOX_EXPANSION = new Attribute("phantazm.hitbox_expand", 0.35F, 2048F, false);
    /**
     * Multiplier applied to the fire rate when shooting guns.
     */
    public static final Attribute FIRE_RATE_MULTIPLIER = new Attribute("phantazm.fire_rate", 1F, 2048F, false);
    /**
     * Revive speed, in ticks.
     */
    public static final Attribute REVIVE_TICKS = new Attribute("phantazm.revive_ticks", 30F, 2048F, false);
    /**
     * Number of ticks between each heal.
     */
    public static final Attribute HEAL_TICKS = new Attribute("phantazm.heal_rate", 20F, 2048F, false);
    /**
     * Damage multiplier for the player.
     */
    public static final Attribute DAMAGE_MULTIPLIER = new Attribute("phantazm.damage_multiplier", 1F, 2048F, false);
    /**
     * Attack speed multiplier for mobs.
     */
    public static final Attribute ATTACK_SPEED_MULTIPLIER =
        new Attribute("phantazm.attack_speed_multiplier", 1F, 2048F, false);
    /**
     * When applied to a mob, modifies any headshot damage received.
     */
    public static final Attribute HEADSHOT_DAMAGE_MULTIPLIER =
        new Attribute("phantazm.headshot_multiplier", 1F, 2048F, false);
    /**
     * The "nil" attribute. Used as a fallback when the desired attribute cannot be found.
     */
    public static final Attribute NIL = new Attribute("phantazm.nil", 0F, 0F, false);

    private Attributes() {
    }

    public static void registerAll() {
        HITBOX_EXPANSION.register();
        FIRE_RATE_MULTIPLIER.register();
        REVIVE_TICKS.register();
        HEAL_TICKS.register();
        DAMAGE_MULTIPLIER.register();
        ATTACK_SPEED_MULTIPLIER.register();
        HEADSHOT_DAMAGE_MULTIPLIER.register();
        NIL.register();
    }
}
