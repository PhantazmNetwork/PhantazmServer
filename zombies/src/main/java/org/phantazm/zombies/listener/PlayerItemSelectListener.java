package org.phantazm.zombies.listener;

import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.Map;
import java.util.function.Supplier;

public class PlayerItemSelectListener extends ZombiesPlayerEventListener<PlayerChangeHeldSlotEvent> {

    public PlayerItemSelectListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
    }

    @Override
    protected void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerChangeHeldSlotEvent event) {
        if (!zombiesPlayer.canUseEquipment()) {
            event.setCancelled(true);
            return;
        }

        InventoryAccessRegistry accessRegistry = zombiesPlayer.module().getInventoryAccessRegistry();
        accessRegistry.getCurrentAccess().ifPresent(inventoryAccess -> {
            InventoryProfile profile = inventoryAccess.profile();
            if (profile.hasInventoryObject(event.getPlayer().getHeldSlot())) {
                InventoryObject object = profile.getInventoryObject(event.getPlayer().getHeldSlot());
                if (object instanceof Equipment equipment) {
                    equipment.setSelected(false);
                }
            }
            if (profile.hasInventoryObject(event.getSlot())) {
                InventoryObject object = profile.getInventoryObject(event.getSlot());
                if (object instanceof Equipment equipment) {
                    equipment.setSelected(true);
                }
            }
        });
    }
}
