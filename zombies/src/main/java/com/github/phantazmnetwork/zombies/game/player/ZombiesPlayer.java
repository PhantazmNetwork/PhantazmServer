package com.github.phantazmnetwork.zombies.game.player;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.core.player.PlayerView;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.equipment.EquipmentCreator;
import com.github.phantazmnetwork.zombies.equipment.EquipmentHandler;
import com.github.phantazmnetwork.zombies.game.coin.ModifierSource;
import com.github.phantazmnetwork.zombies.game.coin.PlayerCoins;
import com.github.phantazmnetwork.zombies.game.kill.PlayerKills;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateKey;
import com.github.phantazmnetwork.zombies.game.player.state.PlayerStateSwitcher;
import com.github.phantazmnetwork.zombies.game.player.state.ZombiesPlayerState;
import net.minestom.server.scoreboard.Sidebar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public interface ZombiesPlayer extends Activable {

    @NotNull ZombiesPlayerMeta getMeta();

    long getReviveTime();

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

    @NotNull Map<PlayerStateKey<?>, Function<?, ZombiesPlayerState>> getStateFunctions();

    @SuppressWarnings("unchecked")
    default <TContext> boolean setState(@NotNull PlayerStateKey<TContext> stateKey, @NotNull TContext context) {
        Function<TContext, ZombiesPlayerState> stateFunction =
                (Function<TContext, ZombiesPlayerState>)getStateFunctions().get(stateKey);
        if (stateFunction != null) {
            getStateSwitcher().setState(stateFunction.apply(context));
            return true;
        }

        return false;
    }

    @NotNull PlayerView getPlayerView();

    @NotNull Sidebar getSidebar();

    void start();

    @NotNull ModifierSource modifiers();
}
