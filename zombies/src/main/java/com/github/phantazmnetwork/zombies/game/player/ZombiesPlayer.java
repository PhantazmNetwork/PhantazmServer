package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.inventory.InventoryProfile;
import com.github.phantazmnetwork.core.inventory.InventoryProfileSwitcher;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Optional;

public interface ZombiesPlayer extends Tickable {

    boolean isCrouching();

    void setCrouching(boolean crouching);

    boolean isInGame();

    void setInGame(boolean inGame);

    void setReviving(boolean reviving);

    boolean isReviving();

    @NotNull PlayerCoins getCoins();

    @NotNull PlayerKills getKills();

    @NotNull @UnmodifiableView Collection<Equipment> getEquipment();

    default @NotNull Optional<Equipment> getHeldEquipment() {
        return getPlayerView().getPlayer().map(player -> {
            InventoryProfileSwitcher profileSwitcher = getProfileSwitcher();
            if (profileSwitcher.hasCurrentProfile()) {
                InventoryProfile profile = profileSwitcher.getCurrentProfile();
                int slot = player.getHeldSlot();
                if (profile.hasInventoryObject(slot)) {
                    InventoryObject object = profile.getInventoryObject(slot);
                    if (object instanceof Equipment equipment) {
                        return equipment;
                    }
                }
            }

            return null;
        });
    }

    @NotNull InventoryProfileSwitcher getProfileSwitcher();

    @NotNull PlayerStateSwitcher getStateSwitcher();

    @NotNull PlayerView getPlayerView();

}
