package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.player.ZombiesPlayer;

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
