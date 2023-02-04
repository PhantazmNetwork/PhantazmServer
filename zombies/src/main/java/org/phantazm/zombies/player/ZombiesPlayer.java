package org.phantazm.zombies.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.core.inventory.InventoryAccess;
import org.phantazm.core.inventory.InventoryAccessRegistry;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.equipment.Equipment;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.Flaggable;
import org.phantazm.zombies.map.MapSettingsInfo;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.state.PlayerStateKey;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.scene.ZombiesScene;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public interface ZombiesPlayer extends Activable, Flaggable.Source, Audience {

    @NotNull ZombiesPlayerModule module();

    long getReviveTime();

    double getReviveSpeedMultiplier();

    void setReviveSpeedMultiplier(double multiplier);

    @NotNull ZombiesScene getScene();

    default @NotNull Optional<Equipment> getHeldEquipment() {
        return module().getPlayerView().getPlayer().map(player -> {
            InventoryAccessRegistry profileSwitcher = module().getInventoryAccessRegistry();
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
                (Function<TContext, ZombiesPlayerState>)module().getStateFunctions().get(stateKey);
        if (stateFunction != null) {
            module().getStateSwitcher().setState(stateFunction.apply(context));
            return true;
        }

        return false;
    }

    default boolean isState(@NotNull PlayerStateKey<?> stateKey) {
        return module().getStateSwitcher().getState().key().equals(stateKey.key());
    }

    default boolean isAlive() {
        return isState(ZombiesPlayerStateKeys.ALIVE);
    }

    default boolean isDead() {
        return isState(ZombiesPlayerStateKeys.DEAD);
    }

    default boolean hasQuit() {
        return isState(ZombiesPlayerStateKeys.QUIT);
    }

    default @NotNull Optional<Player> getPlayer() {
        return module().getPlayerView().getPlayer();
    }

    default @NotNull UUID getUUID() {
        return module().getPlayerView().getUUID();
    }

    default boolean canPickupPowerup(@NotNull Powerup powerup) {
        return isAlive();
    }

    default boolean canOpenDoor(@NotNull Door door) {
        return isAlive();
    }

    default boolean canRepairWindow() {
        return isAlive();
    }

    default boolean canTakeDamage() {
        return isAlive();
    }

    default boolean canBeTargeted() {
        return isAlive();
    }

    default boolean inStage(@NotNull Key stageKey) {
        return getScene().getCurrentStage().key().equals(stageKey);
    }

    interface Source {

        @NotNull ZombiesPlayer createPlayer(@NotNull ZombiesScene scene,
                @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
                @NotNull MapSettingsInfo mapSettingsInfo, @NotNull Instance instance, @NotNull PlayerView playerView,
                @NotNull TransactionModifierSource mapTransactionModifierSource, @NotNull Flaggable flaggable,
                @NotNull EventNode<Event> eventNode, @NotNull Random random, @NotNull MapObjects mapObjects,
                @NotNull MobStore mobStore, @NotNull MobSpawner mobSpawner);

    }

}
