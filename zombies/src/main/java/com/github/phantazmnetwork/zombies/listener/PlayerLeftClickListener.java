package com.github.phantazmnetwork.zombies.listener;

import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.inventory.InventoryProfile;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.player.ZombiesPlayer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;

public class PlayerLeftClickListener extends ZombiesPlayerEventListener<PlayerHandAnimationEvent> {
    public PlayerLeftClickListener(@NotNull Instance instance,
            @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers) {
        super(instance, zombiesPlayers);
    }

    @Override
    protected void accept(@NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerHandAnimationEvent event) {
        if (event.getHand() != Player.Hand.MAIN) {
            return;
        }

        InventoryAccessRegistry profileSwitcher = zombiesPlayer.getModule().getInventoryAccessRegistry();
        if (!profileSwitcher.hasCurrentAccess()) {
            return;
        }

        InventoryAccess access = profileSwitcher.getCurrentAccess();
        InventoryProfile profile = access.profile();
        if (!profile.hasInventoryObject(event.getPlayer().getHeldSlot())) {
            return;
        }

        InventoryObject object = profile.getInventoryObject(event.getPlayer().getHeldSlot());
        if (!(object instanceof Equipment equipment)) {
            return;
        }

        equipment.leftClick();
    }
}
