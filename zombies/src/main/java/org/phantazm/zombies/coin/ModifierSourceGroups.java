package org.phantazm.zombies.coin;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class ModifierSourceGroups {
    public static Key COIN_GAIN = Key.key(Namespaces.PHANTAZM, "coin_gain");

    public static Key MOB_COIN_GAIN = Key.key(Namespaces.PHANTAZM, "coin_gain.mob");

    public static Key WINDOW_COIN_GAIN = Key.key(Namespaces.PHANTAZM, "coin_gain.window");

    public static Key DOOR_COIN_LOSS = Key.key(Namespaces.PHANTAZM, "coin_loss.door");
}
