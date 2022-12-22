package org.phantazm.zombies.map;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class MapFlags {
    public static final Key POWER = Key.key(Namespaces.PHANTAZM, "zombies.map.flag.power");

    private MapFlags() {
        throw new UnsupportedOperationException();
    }
}
