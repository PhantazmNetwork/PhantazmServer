package org.phantazm.zombies.perk;

import net.kyori.adventure.key.Keyed;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.zombies.upgrade.UpgradeNode;

public interface PerkLevel extends UpgradeNode, InventoryObject, Keyed {
    void start();

    void end();
}
