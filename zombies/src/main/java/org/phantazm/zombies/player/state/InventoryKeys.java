package org.phantazm.zombies.player.state;

import net.kyori.adventure.key.Key;
import org.phantazm.commons.Namespaces;

public final class InventoryKeys {
    public static final Key DEFAULT_ACCESS = Key.key(Namespaces.PHANTAZM, "inventory.access.default");

    public static final Key DEAD_ACCESS = Key.key(Namespaces.PHANTAZM, "inventory.access.dead");

    public static final Key GUN_INVENTORY_GROUP = Key.key(Namespaces.PHANTAZM, "inventory.group.gun");

    public static final Key PERK_INVENTORY_GROUP = Key.key(Namespaces.PHANTAZM, "inventory.group.perk");
}
