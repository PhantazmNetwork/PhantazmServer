package org.phantazm.zombies.listener;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.player.ZombiesPlayer;

public class PlayerRightClickListener {

    public void onRightClick(@NotNull ZombiesPlayer player, int slot) {
        if (!player.canUseEquipment()) {
            return;
        }

        player.module().getInventoryAccessRegistry().getCurrentAccess().ifPresent(inventoryAccess -> {
            InventoryProfile profile = inventoryAccess.profile();
            if (!profile.hasInventoryObject(slot)) {
                return;
            }

            InventoryObject object = profile.getInventoryObject(slot);
            if (!(object instanceof Equipment equipment)) {
                return;
            }

            equipment.rightClick();
        });
    }

}
