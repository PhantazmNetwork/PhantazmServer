package org.phantazm.core;

import net.minestom.server.attribute.Attribute;

public final class Attributes {
    private Attributes() {
    }

    public static final Attribute HITBOX_EXPANSION = new Attribute("phantazm.hitbox_expand", 0.15F, 1F);

    public static void registerAll() {
        HITBOX_EXPANSION.register();
    }
}
