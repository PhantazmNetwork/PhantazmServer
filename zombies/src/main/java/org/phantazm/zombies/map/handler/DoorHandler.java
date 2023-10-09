package org.phantazm.zombies.map.handler;

import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.player.ZombiesPlayer;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

public interface DoorHandler {
    @NotNull
    BoundedTracker<Door> doorTracker();

    boolean handleRightClick(@NotNull ZombiesPlayer player, @NotNull Point clicked);

    interface Source {
        @NotNull
        DoorHandler make(@NotNull BoundedTracker<Door> doorTracker, @NotNull BoundedTracker<Room> roomTracker,
            @NotNull Supplier<ZombiesScene> zombiesScene);
    }
}
