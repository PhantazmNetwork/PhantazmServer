package org.phantazm.zombies;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public class Flags {
    public static final Key INSTA_KILL = Key.key(Namespaces.PHANTAZM, "zombies.map.flag.insta_kill");
    public static final Key GODMODE = Key.key(Namespaces.PHANTAZM, "zombies.player.flag.godmode");
    public static final Key BOMBED_ROOM = Key.key(Namespaces.PHANTAZM, "zombies.map.room.flag.bombed");
    public static final Key WALLSHOOTING_ENABLED =
        Key.key(Namespaces.PHANTAZM, "zombies.map.flag.wallshooting_enabled");

    private Flags() {
    }
}
