package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.api.inventory.InventoryObject;
import com.github.phantazmnetwork.commons.Tickable;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;

public interface ZombiesPlayer extends Tickable {
    void setCrouching(boolean crouching);

    int getCoins();

    void setCoins(int amount);

    default void addCoins(int amount) {
        setCoins(getCoins() + amount);
    }

    @NotNull @UnmodifiableView Collection<Equipment> getEquipment();

    @NotNull @UnmodifiableView Collection<InventoryObject> getInventoryObjects();
}
