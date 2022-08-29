package com.github.phantazmnetwork.zombies.game.perk;

import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import com.github.phantazmnetwork.zombies.upgrade.UpgradeNode;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;

public interface PerkLevel extends UpgradeNode, InventoryObject, Keyed {
    void start(@NotNull ZombiesPlayer player);

    void end(@NotNull ZombiesPlayer player);
}
