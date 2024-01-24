package org.phantazm.zombies;

import net.kyori.adventure.text.Component;
import net.minestom.server.tag.Tag;
import org.phantazm.core.TagUtils;

import java.util.List;

public final class Tags {
    public static final Tag<List<String>> POWERUP_TAG = Tag.String(TagUtils.uniqueTagName()).list().defaultValue(List.of());
    public static final Tag<Integer> ARMOR_TIER = Tag.Integer(TagUtils.uniqueTagName()).defaultValue(-1);
    public static final Tag<Long> LAST_ENTER_BOMBED_ROOM = Tag.Long(TagUtils.uniqueTagName()).defaultValue(-1L);
    public static final Tag<Component> DAMAGE_NAME = Tag.Component(TagUtils.uniqueTagName());

    private Tags() {
        throw new UnsupportedOperationException();
    }
}