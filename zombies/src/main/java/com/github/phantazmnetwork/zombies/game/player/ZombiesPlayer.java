package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.core.inventory.InventoryProfileSwitcher;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public interface ZombiesPlayer extends Tickable {
    void setCrouching(boolean crouching);

    @NotNull PlayerCoins getCoins();

    @NotNull @UnmodifiableView Collection<Equipment> getEquipment();

    @NotNull InventoryProfileSwitcher getProfileSwitcher();

    @NotNull PlayerView getPlayerView();

}
