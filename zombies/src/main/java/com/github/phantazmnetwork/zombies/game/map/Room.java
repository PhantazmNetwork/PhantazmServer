package com.github.phantazmnetwork.zombies.game.map;

import com.github.phantazmnetwork.commons.vector.Region3I;
import com.github.phantazmnetwork.commons.vector.Vec3I;
import com.github.phantazmnetwork.zombies.game.map.action.Action;
import com.github.phantazmnetwork.zombies.map.RoomInfo;
import net.minestom.server.instance.Instance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Room extends PositionalMapObject<RoomInfo> {
    private final List<Action<Room>> openActions;
    private final List<Region3I> unmodifiableRegions;
    private boolean isOpen;

    /**
     * Constructs a new instance of this class.
     *
     * @param roomInfo the backing data object
     * @param instance the instance which this MapObject is in
     */
    public Room(@NotNull RoomInfo roomInfo, @NotNull Instance instance, @NotNull List<Action<Room>> openActions) {
        super(roomInfo, Vec3I.floored(Region3I.enclosing(roomInfo.regions().toArray(new Region3I[0])).getCenter()),
                instance);
        this.openActions = List.copyOf(openActions);

        List<Region3I> list = new ArrayList<>(roomInfo.regions().size());
        for (Region3I region : roomInfo.regions()) {
            list.add(region.add(origin));
        }

        this.unmodifiableRegions = Collections.unmodifiableList(list);
    }

    public @UnmodifiableView @NotNull List<Region3I> roomBounds() {
        return unmodifiableRegions;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() {
        if (isOpen) {
            return;
        }

        isOpen = true;
        for (Action<Room> action : openActions) {
            action.perform(this);
        }
    }
}
