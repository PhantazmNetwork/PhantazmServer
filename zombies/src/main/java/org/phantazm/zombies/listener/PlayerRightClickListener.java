package org.phantazm.zombies.listener;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.player.ZombiesPlayer;

public class PlayerRightClickListener {

    public void onRightClick(@NotNull ZombiesPlayer player, int slot) {
        if (player.isAlive()) {
            InventoryAccessRegistry profileSwitcher = player.module().getInventoryAccessRegistry();
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

}
