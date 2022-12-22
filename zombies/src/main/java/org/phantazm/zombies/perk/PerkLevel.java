package org.phantazm.zombies.perk;

import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.upgrade.UpgradeNode;

public interface PerkLevel extends UpgradeNode, InventoryObject, Keyed {
    void start(@NotNull ZombiesPlayer zombiesPlayer);

    void end(@NotNull ZombiesPlayer zombiesPlayer);
}
