package org.phantazm.zombies.map.handler;

import com.github.steanky.toolkit.collection.Wrapper;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.coin.*;
import org.phantazm.zombies.event.player.ZombiesPlayerOpenDoorEvent;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.DoorInfo;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;
import org.phantazm.zombies.stage.StageKeys;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class BasicDoorHandler implements DoorHandler {
    private final BoundedTracker<Door> doorTracker;
    private final BoundedTracker<Room> roomTracker;
    private final Supplier<ZombiesScene> zombiesScene;

    public BasicDoorHandler(@NotNull BoundedTracker<Door> doorTracker, @NotNull BoundedTracker<Room> roomTracker,
        @NotNull Supplier<ZombiesScene> zombiesScene) {
        this.doorTracker = Objects.requireNonNull(doorTracker);
        this.roomTracker = Objects.requireNonNull(roomTracker);
        this.zombiesScene = Objects.requireNonNull(zombiesScene);
    }

    @Override
    public @NotNull BoundedTracker<Door> doorTracker() {
        return doorTracker;
    }

    @Override
    public boolean handleRightClick(@NotNull ZombiesPlayer player, @NotNull Point clicked) {
        if (!player.inStage(StageKeys.IN_GAME)) {
            return false;
        }

        Wrapper<Boolean> handled = Wrapper.of(false);
        doorTracker.atPoint(clicked).ifPresent(door -> {
            if (door.isOpen() || !player.canOpenDoor(door)) {
                return;
            }

            Optional<Player> playerOptional = player.getPlayer();
            if (playerOptional.isEmpty()) {
                return;
            }

            Player actualPlayer = playerOptional.get();
            Optional<Room> standingInRoomOptional = roomTracker.atPoint(actualPlayer.getPosition());
            if (standingInRoomOptional.isEmpty() || !standingInRoomOptional.get().isOpen()) {
                return;
            }

            Room standingInRoom = standingInRoomOptional.get();
            if (!standingInRoom.isOpen()) {
                return;
            }

            ZombiesScene zombiesScene = this.zombiesScene.get();
            boolean anyOpen = false;
            for (Key otherRoom : door.doorInfo().opensTo()) {
                Room room = zombiesScene.map().objects().roomMap().get(otherRoom);
                if (room != null && room.isOpen()) {
                    anyOpen = true;
                    break;
                }
            }

            if (!anyOpen) {
                // can't open rooms that don't have any adjacent rooms open
                return;
            }

            ZombiesPlayerOpenDoorEvent event = new ZombiesPlayerOpenDoorEvent(actualPlayer, player, door);
            zombiesScene.broadcastEvent(event);
            if (event.isCancelled()) {
                return;
            }

            Key standingIn = standingInRoom.getRoomInfo().id();

            DoorInfo info = door.doorInfo();
            List<Key> opensTo = info.opensTo();
            List<Integer> costs = info.costs();

            int sumCost = 0;
            int minSize = Math.min(opensTo.size(), costs.size());
            for (int i = 0; i < minSize; i++) {
                Key target = opensTo.get(i);
                if (target.equals(standingIn)) {
                    continue;
                }

                sumCost += costs.get(i);
            }

            PlayerCoins coins = player.module().getCoins();
            TransactionModifierSource modifiers = player.module().compositeTransactionModifiers();
            TransactionResult result = coins.runTransaction(
                new Transaction(modifiers.modifiers(ModifierSourceGroups.DOOR_COIN_SPEND), -sumCost));

            if (result.isAffordable(coins)) {
                coins.applyTransaction(result);
                door.open(player);
            } else {
                door.failOpen(player);
            }

            handled.set(true);
        });

        return handled.get();
    }
}
