package org.phantazm.zombies.player;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.phantazm.commons.Activable;
import org.phantazm.commons.CancellableState;
import org.phantazm.core.equipment.Equipment;
import org.phantazm.core.inventory.InventoryObject;
import org.phantazm.core.inventory.InventoryProfile;
import org.phantazm.core.player.PlayerView;
import org.phantazm.mob.MobStore;
import org.phantazm.mob.spawner.MobSpawner;
import org.phantazm.zombies.coin.TransactionModifierSource;
import org.phantazm.zombies.corpse.CorpseCreator;
import org.phantazm.zombies.map.*;
import org.phantazm.zombies.map.objects.MapObjects;
import org.phantazm.zombies.player.state.PlayerStateKey;
import org.phantazm.zombies.player.state.ZombiesPlayerState;
import org.phantazm.zombies.player.state.ZombiesPlayerStateKeys;
import org.phantazm.zombies.powerup.Powerup;
import org.phantazm.zombies.scene.ZombiesScene;
import org.phantazm.zombies.stage.Stage;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

public interface ZombiesPlayer extends Activable, Flaggable.Source, Audience {

    @NotNull ZombiesPlayerModule module();

    long getReviveTime();

    @NotNull ZombiesScene getScene();

    void registerCancellable(@NotNull CancellableState cancellable, boolean endOld);

    void removeCancellable(@NotNull UUID id);

    default @NotNull Optional<Equipment> getHeldEquipment() {
        Optional<Player> playerOptional = module().getPlayerView().getPlayer();
        if (playerOptional.isEmpty()) {
            return Optional.empty();
        }

        Player player = playerOptional.get();
        return module().getInventoryAccessRegistry().getCurrentAccess().map(inventoryAccess -> {
            int slot = player.getHeldSlot();
            InventoryProfile profile = inventoryAccess.profile();
            if (profile.hasInventoryObject(slot)) {
                InventoryObject object = profile.getInventoryObject(slot);
                if (object instanceof Equipment equipment) {
                    return equipment;
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

    default boolean isKnocked() {
        return isState(ZombiesPlayerStateKeys.KNOCKED);
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
        return canDoGenericActions();
    }

    default boolean canOpenDoor(@NotNull Door door) {
        return canDoGenericActions();
    }

    default boolean canRepairWindow() {
        return canDoGenericActions();
    }

    default boolean canTakeDamage() {
        return canDoGenericActions();
    }

    default boolean canBeTargeted() {
        return canDoGenericActions() && getPlayer().map(player -> {
            GameMode mode = player.getGameMode();
            return mode == GameMode.SURVIVAL || mode == GameMode.ADVENTURE;
        }).orElse(false);
    }

    default boolean canDoGenericActions() {
        return isAlive() && isInGame();
    }

    default boolean canRevive() {
        if (!canDoGenericActions()) {
            return false;
        }

        return getPlayer().filter(Player::isSneaking).isPresent();
    }

    default boolean canTriggerSLA() {
        return canDoGenericActions();
    }

    default boolean isInGame() {
        return module().getMeta().isInGame();
    }

    default boolean inStage(@NotNull Key stageKey) {
        Stage currentStage = getScene().getCurrentStage();
        if (currentStage == null) {
            return false;
        }

        return currentStage.key().equals(stageKey);
    }

    default @NotNull String getUsername() {
        PlayerView view = module().getPlayerView();
        try {
            return view.getUsername().get();
        }
        catch (InterruptedException | ExecutionException e) {
            return view.getUUID().toString();
        }
    }

    interface Source {

        @NotNull ZombiesPlayer createPlayer(@NotNull ZombiesScene scene,
                @NotNull Map<? super UUID, ? extends ZombiesPlayer> zombiesPlayers,
                @NotNull MapSettingsInfo mapSettingsInfo, @NotNull PlayerCoinsInfo playerCoinsInfo,
                @NotNull LeaderboardInfo leaderboardInfo, @NotNull Instance instance, @NotNull PlayerView playerView,
                @NotNull TransactionModifierSource mapTransactionModifierSource, @NotNull Flaggable flaggable,
                @NotNull EventNode<Event> eventNode, @NotNull Random random, @NotNull MapObjects mapObjects,
                @NotNull MobStore mobStore, @NotNull MobSpawner mobSpawner, @NotNull CorpseCreator corpseCreator);

    }

}
