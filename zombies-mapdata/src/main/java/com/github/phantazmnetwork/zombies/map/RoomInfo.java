package com.github.phantazmnetwork.zombies.map;

import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.vector.Bounds3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Defines a room.
 */
public record RoomInfo(@NotNull Key id,
                       @NotNull Component displayName,
                       @NotNull List<Bounds3I> regions,
                       @NotNull ConfigList openActions) {
    /**
     * Creates a new instance of this record.
     *
     * @param id          the id of the room
     * @param displayName the display name of the room
     * @param regions     the regions that make up the room
     */
    public RoomInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(regions, "regions");
        Objects.requireNonNull(openActions, "openActions");
    }
}
