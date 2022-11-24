package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.inventory.InventoryProfile;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlayerItemSelectListener extends ZombiesPlayerEventListener<PlayerChangeHeldSlotEvent> {

    public PlayerItemSelectListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        super(instance, zombiesPlayers);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerChangeHeldSlotEvent event) {
        InventoryAccessRegistry accessRegistry = zombiesPlayer.getModule().getInventoryAccessRegistry();
        if (accessRegistry.hasCurrentAccess()) {
            InventoryProfile profile = accessRegistry.getCurrentAccess().profile();
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
        }
    }
}
