package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;
import java.util.function.Supplier;

public class BasicZombiesPlayer implements ZombiesPlayer {

    private final PlayerView playerView;

    private final ZombiesPlayerMeta meta;

    private final PlayerCoins coins;

    private final PlayerKills kills;

    private final EquipmentHandler equipmentHandler;

    private final EquipmentCreator equipmentCreator;

    private final InventoryAccessRegistry profileSwitcher;

    private final PlayerStateSwitcher stateSwitcher;

    private final Map<Key, Supplier<ZombiesPlayerState>> stateSuppliers;

    private final Sidebar sidebar;

    public BasicZombiesPlayer(@NotNull PlayerView playerView, @NotNull ZombiesPlayerMeta meta,
            @NotNull PlayerCoins coins, @NotNull PlayerKills kills, @NotNull EquipmentHandler equipmentHandler,
            @NotNull EquipmentCreator equipmentCreator, @NotNull InventoryAccessRegistry profileSwitcher,
            @NotNull PlayerStateSwitcher stateSwitcher, @NotNull Map<Key, Supplier<ZombiesPlayerState>> stateSuppliers,
            @NotNull Sidebar sidebar) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.coins = Objects.requireNonNull(coins, "coins");
        this.kills = Objects.requireNonNull(kills, "kills");
        this.equipmentHandler = Objects.requireNonNull(equipmentHandler, "equipmentHandler");
        this.equipmentCreator = Objects.requireNonNull(equipmentCreator, "equipmentCreator");
        this.profileSwitcher = Objects.requireNonNull(profileSwitcher, "profileSwitcher");
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
        this.stateSuppliers = Map.copyOf(stateSuppliers);
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
    }

    @Override
    public void tick(long time) {
        Optional<Player> playerOptional = playerView.getPlayer();
        if (playerOptional.isPresent()) {
            meta.setCrouching(playerOptional.get().getPose() == Entity.Pose.SLEEPING);
        }
        else {
            meta.setCrouching(false);
        }

        stateSwitcher.tick(time);
    }

    @Override
    public @NotNull ZombiesPlayerMeta getMeta() {
        return meta;
    }

    @Override
    public long getReviveTime() {
        return 30L;// todo: fast revive
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
    public @NotNull EquipmentHandler getEquipmentHandler() {
        return equipmentHandler;
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
    public @NotNull Map<Key, Supplier<ZombiesPlayerState>> getStateSuppliers() {
        return stateSuppliers;
    }

    @Override
    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    @Override
    public @NotNull Sidebar getSidebar() {
        return sidebar;
    }

    @Override
    public void start() {
        getStateSwitcher().start();
    }

}
