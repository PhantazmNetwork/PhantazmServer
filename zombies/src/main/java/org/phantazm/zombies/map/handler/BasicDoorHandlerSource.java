package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.Room;
import org.phantazm.zombies.scene2.ZombiesScene;

import java.util.function.Supplier;

public class BasicDoorHandlerSource implements DoorHandler.Source {
    @Override
    public @NotNull DoorHandler make(@NotNull BoundedTracker<Door> doorTracker,
        @NotNull BoundedTracker<Room> roomTracker, @NotNull Supplier<ZombiesScene> zombiesScene) {
        return new BasicDoorHandler(doorTracker, roomTracker, zombiesScene);
    }
}
