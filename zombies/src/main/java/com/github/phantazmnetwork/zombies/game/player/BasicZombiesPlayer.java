package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.core.inventory.InventoryProfileSwitcher;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

@SuppressWarnings("ClassCanBeRecord")
public class BasicZombiesPlayer implements ZombiesPlayer {

    private final PlayerView playerView;

    private final PlayerCoins coins;

    private final InventoryProfileSwitcher profileSwitcher;

    public BasicZombiesPlayer(@NotNull PlayerView playerView, @NotNull PlayerCoins coins,
                              @NotNull InventoryProfileSwitcher profileSwitcher) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.coins = Objects.requireNonNull(coins, "coins");
        this.profileSwitcher = Objects.requireNonNull(profileSwitcher, "profileSwitcher");
    }

    @Override
    public void tick(long time) {

    }

    @Override
    public void setCrouching(boolean crouching) {

    }

    @Override
    public @NotNull PlayerCoins getCoins() {
        return coins;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Equipment> getEquipment() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull InventoryProfileSwitcher getProfileSwitcher() {
        return profileSwitcher;
    }

    @Override
    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }
}
