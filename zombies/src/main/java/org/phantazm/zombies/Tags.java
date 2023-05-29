package org.phantazm.zombies;

import net.minestom.server.tag.Tag;

public final class Tags {
    private Tags() {
    }

    public static final Tag<String> POWERUP_TAG = Tag.String("phantazm:powerup");

    public static final Tag<Boolean> INVULNERABILITY_TAG = Tag.Boolean("phantazm:invulnerable").defaultValue(false);
}
