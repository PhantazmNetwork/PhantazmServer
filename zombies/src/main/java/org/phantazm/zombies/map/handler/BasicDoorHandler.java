package org.phantazm.zombies.map.handler;

import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.coin.*;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.DoorInfo;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.player.ZombiesPlayer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class BasicDoorHandler implements DoorHandler {
    private final BoundedTracker<Door> doorTracker;
    private final BoundedTracker<Room> roomTracker;

    public BasicDoorHandler(@NotNull BoundedTracker<Door> doorTracker, @NotNull BoundedTracker<Room> roomTracker) {
        this.doorTracker = Objects.requireNonNull(doorTracker, "doorTracker");
        this.roomTracker = Objects.requireNonNull(roomTracker, "roomTracker");
    }

    @Override
    public @NotNull BoundedTracker<Door> doorTracker() {
        return doorTracker;
    }

    @Override
    public void handleRightClick(@NotNull ZombiesPlayer player, @NotNull Point clicked) {
        doorTracker.atPoint(clicked).ifPresent(door -> {
            Optional<Player> playerOptional = player.getPlayer();
            if (playerOptional.isEmpty()) {
                return;
            }

            Player actualPlayer = playerOptional.get();

            if (!door.isOpen() && player.canOpenDoor(door)) {
                Key standingIn = roomTracker.atPoint(actualPlayer.getPosition()).map(room -> room.getRoomInfo().id())
                        .orElse(null);

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
                        new Transaction(modifiers.modifiers(ModifierSourceGroups.DOOR_COIN_LOSS), -sumCost));

                if (result.isAffordable(coins)) {
                    coins.applyTransaction(result);
                    door.open();
                }
            }
        });
    }
}
