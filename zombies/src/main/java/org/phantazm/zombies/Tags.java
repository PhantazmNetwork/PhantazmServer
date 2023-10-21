package org.phantazm.zombies;

import net.kyori.adventure.text.Component;
import net.minestom.server.tag.Tag;

import java.util.List;

public final class Tags {
    public static final Tag<List<String>> POWERUP_TAG = Tag.String("powerup").list().defaultValue(List.of());
    public static final Tag<Integer> ARMOR_TIER = Tag.Integer("armor_tier").defaultValue(-1);
    public static final Tag<Long> LAST_ENTER_BOMBED_ROOM = Tag.Long("entered_bombed_room").defaultValue(-1L);
    public static final Tag<Component> DAMAGE_NAME = Tag.Component("damage_name");

    private Tags() {
        throw new UnsupportedOperationException();
    }
}