package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.event.player.ZombiesPlayerUseEquipmentEvent;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.Optional;

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

            Optional<Player> playerOptional = player.getPlayer();
            if (playerOptional.isPresent()) {
                ZombiesPlayerUseEquipmentEvent event = new ZombiesPlayerUseEquipmentEvent(player, playerOptional.get(), equipment);
                player.getScene().broadcastEvent(event);

                if (event.isCancelled()) {
                    return;
                }
            }

            equipment.rightClick();
        });
    }

}
