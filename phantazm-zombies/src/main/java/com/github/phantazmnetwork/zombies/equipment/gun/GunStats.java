package com.github.phantazmnetwork.zombies.equipment.gun;

import com.github.phantazmnetwork.commons.Namespaces;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public record GunStats(long shootSpeed,
                       long reloadSpeed,
                       int maxAmmo,
                       int maxClip,
                       int shots,
                       long shotInterval) implements Keyed {

    public static final Key SERIAL_KEY = Key.key(Namespaces.PHANTAZM, "gun.stats");

    @Override
    public @NotNull Key key() {
        return SERIAL_KEY;
    }
}
