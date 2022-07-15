package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class BasicZombiesPlayer implements ZombiesPlayer {

    private final PlayerView playerView;

    public BasicZombiesPlayer(@NotNull PlayerView playerView) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void setCrouching(boolean crouching) {

    }

    @Override
    public int getCoins() {
        return 0;
    }

    @Override
    public void setCoins(int amount) {

    }

    @Override
    public @NotNull @UnmodifiableView Collection<Equipment> getEquipment() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull @UnmodifiableView Collection<InventoryObject> getInventoryObjects() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }
}
