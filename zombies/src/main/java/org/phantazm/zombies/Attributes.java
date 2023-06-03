package org.phantazm.zombies;

import net.minestom.server.attribute.Attribute;

public final class Attributes {
    private Attributes() {
    }

    /**
     * Expansion applied to the hitbox of entities. Used to make things easier (or harder) to hit with bullets.
     */
    public static final Attribute HITBOX_EXPANSION = new Attribute("phantazm.hitbox_expand", 0.15F, 2048F);

    /**
     * Multiplier applied to the fire rate when shooting guns.
     */
    public static final Attribute FIRE_RATE_MULTIPLIER = new Attribute("phantazm.fire_rate", 1F, 2048F);

    /**
     * Revive speed, in ticks.
     */
    public static final Attribute REVIVE_TICKS = new Attribute("phantazm.revive_ticks", 30F, 2048F);

    /**
     * Number of ticks between each heal.
     */
    public static final Attribute HEAL_TICKS = new Attribute("phantazm.heal_rate", 20F, 2048F);

    /**
     * The "nil" attribute. Used as a fallback when the desired attribute cannot be found.
     */
    public static final Attribute NIL = new Attribute("phantazm.nil", 0F, 0F);

    public static void registerAll() {
        HITBOX_EXPANSION.register();
        FIRE_RATE_MULTIPLIER.register();
        REVIVE_TICKS.register();
        HEAL_TICKS.register();
        NIL.register();
    }
}
