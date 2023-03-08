package org.phantazm.zombies.map;

import com.github.steanky.vector.Bounds3I;
import net.minestom.server.coordinate.Point;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.phantazm.core.VecUtils;
import org.phantazm.core.tracker.Bounded;
import org.phantazm.core.tracker.BoundedBase;
import org.phantazm.zombies.map.action.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room extends BoundedBase {
    private final List<Action<Room>> openActions;
    private final RoomInfo roomInfo;
    private volatile boolean isOpen;

    private final Object sync;

    /**
     * Constructs a new instance of this class.
     *
     * @param roomInfo the backing data object
     */
    public Room(@NotNull Point mapOrigin, @NotNull RoomInfo roomInfo, @NotNull List<Action<Room>> openActions) {
        super(mapOrigin, roomInfo.regions().toArray(new Bounds3I[0]));
        this.openActions = List.copyOf(openActions);
        this.roomInfo = Objects.requireNonNull(roomInfo, "roomInfo");
        this.sync = new Object();
    }

    public @NotNull RoomInfo getRoomInfo() {
        return roomInfo;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() {
        synchronized (sync) {
            if (isOpen) {
                return;
            }

            isOpen = true;
            for (Action<Room> action : openActions) {
                action.perform(this);
            }
        }
    }
}
