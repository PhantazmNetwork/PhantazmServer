package org.phantazm.zombies.map.handler;

import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedTracker;
import org.phantazm.zombies.map.Door;
import org.phantazm.zombies.map.Room;

public class BasicDoorHandlerSource implements DoorHandler.Source {
    @Override
    public @NotNull DoorHandler make(@NotNull BoundedTracker<Door> doorTracker,
        @NotNull BoundedTracker<Room> roomTracker) {
        return new BasicDoorHandler(doorTracker, roomTracker);
    }
}
