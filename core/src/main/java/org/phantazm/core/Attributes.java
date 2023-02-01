package org.phantazm.core;

import net.minestom.server.attribute.Attribute;

public class Attributes {
    public static final Attribute HITBOX_EXPANSION = new Attribute("phantazm.hitbox_expand", 0.1F, 1F);

    public static void register() {
        HITBOX_EXPANSION.register();
    }
}
