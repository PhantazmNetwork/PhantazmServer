package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Optional;

public interface ZombiesPlayer extends Tickable {

    boolean isCrouching();

    boolean isInGame();

    void setInGame(boolean inGame);

    boolean isReviving();

    void setReviving(boolean reviving);

    @NotNull PlayerCoins getCoins();

    @NotNull PlayerKills getKills();

    @NotNull EquipmentHandler getEquipmentHandler();

    @NotNull EquipmentCreator getEquipmentCreator();

    @NotNull @UnmodifiableView Collection<Equipment> getEquipment();

    default @NotNull Optional<Equipment> getHeldEquipment() {
        return getPlayerView().getPlayer().map(player -> {
            InventoryAccessRegistry profileSwitcher = getInventoryAccessRegistry();
            if (profileSwitcher.hasCurrentAccess()) {
                InventoryAccess access = profileSwitcher.getCurrentAccess();
                int slot = player.getHeldSlot();
                if (access.profile().hasInventoryObject(slot)) {
                    InventoryObject object = access.profile().getInventoryObject(slot);
                    if (object instanceof Equipment equipment) {
                        return equipment;
                    }
                }
            }

            return null;
        });
    }

    @NotNull InventoryAccessRegistry getInventoryAccessRegistry();

    @NotNull PlayerStateSwitcher getStateSwitcher();

    @NotNull PlayerView getPlayerView();

}
