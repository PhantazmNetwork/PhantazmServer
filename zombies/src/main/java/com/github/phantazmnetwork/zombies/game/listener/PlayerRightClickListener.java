package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.inventory.InventoryProfile;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerRightClickListener {

    public void onRightClick(@NotNull ZombiesPlayer player, int slot) {
        InventoryAccessRegistry profileSwitcher = player.getModule().getInventoryAccessRegistry();
        if (!profileSwitcher.hasCurrentAccess()) {
            return;
        }

        InventoryAccess access = profileSwitcher.getCurrentAccess();
        InventoryProfile profile = access.profile();
        if (!profile.hasInventoryObject(slot)) {
            return;
        }

        InventoryObject object = profile.getInventoryObject(slot);
        if (!(object instanceof Equipment equipment)) {
            return;
        }

        equipment.rightClick();
    }

}
