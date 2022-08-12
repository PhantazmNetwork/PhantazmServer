package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class BasicZombiesPlayer implements ZombiesPlayer {

    private final PlayerView playerView;

    private final PlayerCoins coins;

    private final PlayerKills kills;

    private final EquipmentCreator equipmentCreator;

    private final InventoryAccessRegistry profileSwitcher;

    private final PlayerStateSwitcher stateSwitcher;

    private boolean reviving = false;

    private boolean inGame = false;

    public BasicZombiesPlayer(@NotNull PlayerView playerView, @NotNull PlayerCoins coins, @NotNull PlayerKills kills,
            @NotNull EquipmentCreator equipmentCreator, @NotNull InventoryAccessRegistry profileSwitcher,
            @NotNull PlayerStateSwitcher stateSwitcher) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.coins = Objects.requireNonNull(coins, "coins");
        this.kills = Objects.requireNonNull(kills, "kills");
        this.equipmentCreator = Objects.requireNonNull(equipmentCreator, "equipmentCreator");
        this.profileSwitcher = Objects.requireNonNull(profileSwitcher, "profileSwitcher");
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
    }

    @Override
    public void tick(long time) {
    }

    @Override
    public boolean isCrouching() {
        Optional<Player> playerOptional = playerView.getPlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            return player.getPose() == Entity.Pose.SNEAKING;
        }

        return false;
    }

    @Override
    public boolean isInGame() {
        return inGame;
    }

    @Override
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }

    @Override
    public boolean isReviving() {
        return reviving;
    }

    @Override
    public void setReviving(boolean reviving) {
        this.reviving = reviving;
    }

    @Override
    public @NotNull PlayerCoins getCoins() {
        return coins;
    }

    @Override
    public @NotNull PlayerKills getKills() {
        return kills;
    }

    @Override
    public @NotNull EquipmentCreator getEquipmentCreator() {
        return equipmentCreator;
    }

    @Override
    public @NotNull @UnmodifiableView Collection<Equipment> getEquipment() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull InventoryAccessRegistry getInventoryAccessRegistry() {
        return profileSwitcher;
    }

    @Override
    public @NotNull PlayerStateSwitcher getStateSwitcher() {
        return stateSwitcher;
    }

    @Override
    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }
}
