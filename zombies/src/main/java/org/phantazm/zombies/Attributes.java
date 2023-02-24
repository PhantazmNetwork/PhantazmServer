package org.phantazm.zombies;

import net.minestom.server.attribute.Attribute;

public final class Attributes {
    private Attributes() {
    }

    public static final Attribute HITBOX_EXPANSION = new Attribute("phantazm.hitbox_expand", 0.15F, 1F);

    public static final Attribute FIRE_RATE_MULTIPLIER = new Attribute("phantazm.fire_rate", 1F, 10F);

    public static void registerAll() {
        HITBOX_EXPANSION.register();
        FIRE_RATE_MULTIPLIER.register();
    }
}
