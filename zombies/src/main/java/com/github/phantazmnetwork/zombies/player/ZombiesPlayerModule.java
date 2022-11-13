package com.github.phantazmnetwork.zombies.player;

import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.map.Flaggable;
import com.github.phantazmnetwork.zombies.player.state.PlayerStateKey;
import com.github.phantazmnetwork.zombies.player.state.PlayerStateSwitcher;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerState;
import com.github.steanky.element.core.annotation.DependencySupplier;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ZombiesPlayerModule implements DependencyModule {

    private final PlayerView playerView;

    private final ZombiesPlayerMeta meta;

    private final PlayerCoins coins;

    private final PlayerKills kills;

    private final EquipmentHandler equipmentHandler;

    private final EquipmentCreator equipmentCreator;

    private final InventoryAccessRegistry profileSwitcher;

    private final PlayerStateSwitcher stateSwitcher;

    private final Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions;

    private final Sidebar sidebar;

    private final ModifierSource modifierSource;

    private final Flaggable flaggable;

    public ZombiesPlayerModule(@NotNull PlayerView playerView, @NotNull ZombiesPlayerMeta meta,
            @NotNull PlayerCoins coins, @NotNull PlayerKills kills, @NotNull EquipmentHandler equipmentHandler,
            @NotNull EquipmentCreator equipmentCreator, @NotNull InventoryAccessRegistry profileSwitcher,
            @NotNull PlayerStateSwitcher stateSwitcher,
            @NotNull Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions,
            @NotNull Sidebar sidebar, @NotNull ModifierSource modifierSource, @NotNull Flaggable flaggable) {
        this.playerView = Objects.requireNonNull(playerView, "playerView");
        this.meta = Objects.requireNonNull(meta, "meta");
        this.coins = Objects.requireNonNull(coins, "coins");
        this.kills = Objects.requireNonNull(kills, "kills");
        this.equipmentHandler = Objects.requireNonNull(equipmentHandler, "equipmentHandler");
        this.equipmentCreator = Objects.requireNonNull(equipmentCreator, "equipmentCreator");
        this.profileSwitcher = Objects.requireNonNull(profileSwitcher, "profileSwitcher");
        this.stateSwitcher = Objects.requireNonNull(stateSwitcher, "stateSwitcher");
        this.stateFunctions = Map.copyOf(stateFunctions);
        this.sidebar = Objects.requireNonNull(sidebar, "sidebar");
        this.modifierSource = Objects.requireNonNull(modifierSource, "modifierSource");
        this.flaggable = Objects.requireNonNull(flaggable, "flags");
    }

    @DependencySupplier("zombies.dependency.player.meta")
    public @NotNull ZombiesPlayerMeta getMeta() {
        return meta;
    }

    @DependencySupplier("zombies.dependency.player.coins")
    public @NotNull PlayerCoins getCoins() {
        return coins;
    }

    @DependencySupplier("zombies.dependency.player.kills")
    public @NotNull PlayerKills getKills() {
        return kills;
    }

    @DependencySupplier("zombies.dependency.player.equipment_handler")
    public @NotNull EquipmentHandler getEquipmentHandler() {
        return equipmentHandler;
    }

    @DependencySupplier("zombies.dependency.player.equipment_creator")
    public @NotNull EquipmentCreator getEquipmentCreator() {
        return equipmentCreator;
    }

    @DependencySupplier("zombies.dependency.player.equipment")
    public @NotNull @UnmodifiableView Collection<Equipment> getEquipment() {
        return Collections.emptyList();
    }

    @DependencySupplier("zombies.dependency.player.inventory_access_registry")
    public @NotNull InventoryAccessRegistry getInventoryAccessRegistry() {
        return profileSwitcher;
    }

    @DependencySupplier("zombies.dependency.player.state_switcher")
    public @NotNull PlayerStateSwitcher getStateSwitcher() {
        return stateSwitcher;
    }

    @DependencySupplier("zombies.dependency.player.state_functions")
    public @NotNull Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> getStateFunctions() {
        return stateFunctions;
    }

    @DependencySupplier("zombies.dependency.player.player_view")
    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    @DependencySupplier("zombies.dependency.player.sidebar")
    public @NotNull Sidebar getSidebar() {
        return sidebar;
    }

    @DependencySupplier("zombies.dependency.player.modifiers")
    public @NotNull ModifierSource modifiers() {
        return modifierSource;
    }

    @DependencySupplier("zombies.dependency.player.flaggable")
    public @NotNull Flaggable flaggable() {
        return flaggable;
    }
}
