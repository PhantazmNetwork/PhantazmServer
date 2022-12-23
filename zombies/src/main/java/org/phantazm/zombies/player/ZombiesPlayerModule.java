package org.phantazm.zombies.player;

import com.github.steanky.element.core.annotation.Dependency;
import com.github.steanky.element.core.annotation.Memoize;
import com.github.steanky.element.core.dependency.DependencyModule;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.player.PlayerView;
import org.phantazm.zombies.coin.PlayerCoins;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.equipment.EquipmentCreator;
import org.phantazm.zombies.equipment.EquipmentHandler;
import org.phantazm.zombies.kill.PlayerKills;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.player.state.PlayerStateKey;
import org.phantazm.zombies.player.state.PlayerStateSwitcher;
import org.phantazm.zombies.player.state.ZombiesPlayerState;

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

    private final TransactionModifierSource playerTransactionModifierSource;

    private final TransactionModifierSource compositeTransactionModifierSource;

    private final Flaggable flaggable;

    public ZombiesPlayerModule(@NotNull PlayerView playerView, @NotNull ZombiesPlayerMeta meta,
            @NotNull PlayerCoins coins, @NotNull PlayerKills kills, @NotNull EquipmentHandler equipmentHandler,
            @NotNull EquipmentCreator equipmentCreator, @NotNull InventoryAccessRegistry profileSwitcher,
            @NotNull PlayerStateSwitcher stateSwitcher,
            @NotNull Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> stateFunctions,
            @NotNull Sidebar sidebar, @NotNull TransactionModifierSource mapTransactionModifierSource,
            @NotNull TransactionModifierSource playerTransactionModifierSource, @NotNull Flaggable flaggable) {
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
        this.playerTransactionModifierSource =
                Objects.requireNonNull(playerTransactionModifierSource, "playerTransactionModifierSource");
        this.compositeTransactionModifierSource =
                TransactionModifierSource.compositeView(mapTransactionModifierSource, playerTransactionModifierSource);
        this.flaggable = Objects.requireNonNull(flaggable, "flags");
    }

    @Dependency
    @Memoize
    public @NotNull ZombiesPlayerMeta getMeta() {
        return meta;
    }

    @Dependency
    @Memoize
    public @NotNull PlayerCoins getCoins() {
        return coins;
    }

    @Dependency
    @Memoize
    public @NotNull PlayerKills getKills() {
        return kills;
    }

    @Dependency
    @Memoize
    public @NotNull EquipmentHandler getEquipmentHandler() {
        return equipmentHandler;
    }

    @Dependency
    @Memoize
    public @NotNull EquipmentCreator getEquipmentCreator() {
        return equipmentCreator;
    }

    @Dependency
    @Memoize
    public @NotNull @UnmodifiableView Collection<Equipment> getEquipment() {
        return Collections.emptyList();
    }

    @Dependency
    @Memoize
    public @NotNull InventoryAccessRegistry getInventoryAccessRegistry() {
        return profileSwitcher;
    }

    @Dependency
    @Memoize
    public @NotNull PlayerStateSwitcher getStateSwitcher() {
        return stateSwitcher;
    }

    @Dependency
    @Memoize
    public @NotNull Map<PlayerStateKey<?>, Function<?, ? extends ZombiesPlayerState>> getStateFunctions() {
        return stateFunctions;
    }

    @Dependency
    @Memoize
    public @NotNull PlayerView getPlayerView() {
        return playerView;
    }

    @Dependency
    @Memoize
    public @NotNull Sidebar getSidebar() {
        return sidebar;
    }

    @Dependency("zombies.dependency.player.modifiers")
    @Memoize
    public @NotNull TransactionModifierSource playerTransactionModifiers() {
        return playerTransactionModifierSource;
    }

    @Dependency("zombies.dependency.player.composite_modifiers")
    @Memoize
    public @NotNull TransactionModifierSource compositeTransactionModifiers() {
        return compositeTransactionModifierSource;
    }

    @Dependency
    @Memoize
    public @NotNull Flaggable flaggable() {
        return flaggable;
    }
}
