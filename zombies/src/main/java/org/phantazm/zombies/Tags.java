package org.phantazm.zombies;

import net.minestom.server.tag.Tag;
import org.phantazm.zombies.equipment.gun.Gun;

public final class Tags {
    private Tags() {
    }

    public static Tag<Boolean> HAS_INSTAKILL = Tag.Boolean("phantazm.has_instakill").defaultValue(false);

    public static Tag<Boolean> RESIST_INSTAKILL = Tag.Boolean("phantazm.resist_instakill").defaultValue(false);
}
