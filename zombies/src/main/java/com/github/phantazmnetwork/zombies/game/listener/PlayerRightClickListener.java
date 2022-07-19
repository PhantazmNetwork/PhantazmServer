package com.github.phantazmnetwork.zombies.game.listener;

import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.inventory.InventoryProfile;
import com.github.phantazmnetwork.core.inventory.InventoryProfileSwitcher;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.game.player.ZombiesPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerRightClickListener {

    public void onRightClick(@NotNull ZombiesPlayer player, int slot) {
        InventoryProfileSwitcher profileSwitcher = player.getProfileSwitcher();
        if (!profileSwitcher.hasCurrentProfile()) {
            return;
        }

        InventoryProfile profile = profileSwitcher.getCurrentProfile();
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
