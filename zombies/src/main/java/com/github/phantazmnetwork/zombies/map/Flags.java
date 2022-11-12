package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;

public final class Flags {
    public static final Key POWER = Key.key(Namespaces.PHANTAZM, "zombies.map.flag.power");

    private Flags() {
        throw new UnsupportedOperationException();
    }
}
