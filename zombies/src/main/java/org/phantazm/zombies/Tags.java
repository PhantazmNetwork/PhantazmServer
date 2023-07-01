package org.phantazm.zombies;

import net.minestom.server.tag.Tag;

import java.util.UUID;

public final class Tags {
    private Tags() {
    }

    public static final Tag<String> POWERUP_TAG = Tag.String("powerup");

    public static final Tag<UUID> LAST_HIT_BY = Tag.UUID("last_hit");

    public static final Tag<Integer> ARMOR_TIER = Tag.Integer("armor_tier").defaultValue(-1);

    public static final Tag<Long> LAST_ENTER_BOMBED_ROOM = Tag.Long("entered_bombed_room").defaultValue(-1L);
}
