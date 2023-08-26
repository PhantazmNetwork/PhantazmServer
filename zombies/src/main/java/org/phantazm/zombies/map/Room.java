package org.phantazm.zombies.map;

import com.github.steanky.vector.Bounds3I;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.zombies.map.action.Action;

import java.util.List;
import java.util.Objects;

public class Room extends BoundedBase {
    private static final Bounds3I[] EMPTY_BOUNDS_ARRAY = new Bounds3I[0];

    private final List<Action<Room>> openActions;
    private final RoomInfo roomInfo;
    private final Object sync;
    private final Flaggable flaggable;
    private volatile boolean isOpen;

    /**
     * Constructs a new instance of this class.
     *
     * @param roomInfo the backing data object
     */
    public Room(@NotNull Point mapOrigin, @NotNull RoomInfo roomInfo, @NotNull List<Action<Room>> openActions) {
        super(mapOrigin, roomInfo.regions().toArray(EMPTY_BOUNDS_ARRAY));
        this.openActions = List.copyOf(openActions);
        this.roomInfo = Objects.requireNonNull(roomInfo);
        this.sync = new Object();
        this.flaggable = new BasicFlaggable();
    }

    public @NotNull RoomInfo getRoomInfo() {
        return roomInfo;
    }

    public boolean isOpen() {
        return isOpen || roomInfo.isSpawn();
    }

    public void open() {
        synchronized (sync) {
            if (isOpen()) {
                return;
            }

            isOpen = true;
            for (Action<Room> action : openActions) {
                action.perform(this);
            }
        }
    }

    public @NotNull Flaggable flags() {
        return flaggable;
    }
}
