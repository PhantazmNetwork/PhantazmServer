package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.RoomInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Room extends PositionalMapObject<RoomInfo> {
    private boolean isOpen;
    private final List<Action<Room>> openActions;

    /**
     * Constructs a new instance of this class.
     *
     * @param roomInfo the backing data object
     * @param origin   the origin vector this object's coordinates are considered relative to
     * @param instance the instance which this MapObject is in
     */
    public Room(@NotNull RoomInfo roomInfo, @NotNull Vec3I origin, @NotNull Instance instance, boolean isOpen,
                @NotNull List<Action<Room>> openActions) {
        super(roomInfo, origin, instance);
        this.isOpen = isOpen;
        this.openActions = Objects.requireNonNull(openActions, "openActions");

        Collections.sort(openActions);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() {
        if(isOpen) {
            return;
        }

        isOpen = true;
        for(Action<Room> action : openActions) {
            action.perform(this);
        }
    }
}
