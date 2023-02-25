package org.phantazm.zombies.player.state;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class InventoryKeys {
    private InventoryKeys() {

    }

    public static final Key ALIVE_ACCESS = Key.key(Namespaces.PHANTAZM, "inventory.access.alive");

    public static final Key DEAD_ACCESS = Key.key(Namespaces.PHANTAZM, "inventory.access.dead");
}
