package com.github.phantazmnetwork.zombies.player;

import com.github.phantazmnetwork.commons.Activable;
import com.github.phantazmnetwork.core.inventory.InventoryAccess;
import com.github.phantazmnetwork.core.inventory.InventoryAccessRegistry;
import com.github.phantazmnetwork.core.inventory.InventoryObject;
import com.github.phantazmnetwork.zombies.equipment.Equipment;
import com.github.phantazmnetwork.zombies.map.Flaggable;
import com.github.phantazmnetwork.zombies.player.state.PlayerStateKey;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerState;
import com.github.phantazmnetwork.zombies.player.state.ZombiesPlayerStateKeys;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface ZombiesPlayer extends Activable, Flaggable.Source {

    @NotNull ZombiesPlayerModule getModule();

    long getReviveTime();

    default @NotNull Optional<Equipment> getHeldEquipment() {
        return getModule().getPlayerView().getPlayer().map(player -> {
            InventoryAccessRegistry profileSwitcher = getModule().getInventoryAccessRegistry();
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

    @SuppressWarnings("unchecked")
    default <TContext> boolean setState(@NotNull PlayerStateKey<TContext> stateKey, @NotNull TContext context) {
        Function<TContext, ZombiesPlayerState> stateFunction =
                (Function<TContext, ZombiesPlayerState>)getModule().getStateFunctions().get(stateKey);
        if (stateFunction != null) {
            getModule().getStateSwitcher().setState(stateFunction.apply(context));
            return true;
        }

        return false;
    }

    default boolean isState(@NotNull PlayerStateKey<?> stateKey) {
        return getModule().getStateSwitcher().getState().key().equals(stateKey.key());
    }

    default boolean isAlive() {
        return isState(ZombiesPlayerStateKeys.ALIVE);
    }

    default boolean hasQuit() {
        return isState(ZombiesPlayerStateKeys.QUIT);
    }

    default @NotNull Optional<Player> getPlayer() {
        return getModule().getPlayerView().getPlayer();
    }

    default @NotNull UUID getUUID() {
        return getModule().getPlayerView().getUUID();
    }
}
