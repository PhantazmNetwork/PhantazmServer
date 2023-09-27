package org.phantazm.zombies.listener;

import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
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


public class PlayerLeftClickListener extends ZombiesPlayerEventListener<PlayerHandAnimationEvent> {
    public PlayerLeftClickListener(@NotNull Instance instance,
        @NotNull Map<PlayerView, ZombiesPlayer> zombiesPlayers, @NotNull Supplier<ZombiesScene> scene) {
        super(instance, zombiesPlayers, scene);
    }

    @Override
    protected void accept(@NotNull ZombiesScene scene, @NotNull ZombiesPlayer zombiesPlayer, @NotNull PlayerHandAnimationEvent event) {
        if (event.getHand() != Player.Hand.MAIN || zombiesPlayer.blockHandAnimation() || !zombiesPlayer.canUseEquipment()) {
            return;
        }

        InventoryAccessRegistry profileSwitcher = zombiesPlayer.module().getInventoryAccessRegistry();
        profileSwitcher.getCurrentAccess().ifPresent(inventoryAccess -> {
            InventoryProfile profile = inventoryAccess.profile();
            if (!profile.hasInventoryObject(event.getPlayer().getHeldSlot())) {
                return;
            }

            InventoryObject object = profile.getInventoryObject(event.getPlayer().getHeldSlot());
            if (!(object instanceof Equipment equipment)) {
                return;
            }

            equipment.leftClick();
        });
    }
}
