package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.vector.Bounds3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Defines a door.
 */
public record DoorInfo(@NotNull Key id,
                       @NotNull List<Key> opensTo,
                       @NotNull List<Integer> costs,
                       @NotNull List<HologramInfo> holograms,
                       @NotNull List<Bounds3I> regions,
                       @NotNull Sound openSound,
                       @NotNull ConfigList openActions) {

    /**
     * The default sound played when a door opens.
     */
    public static final Sound DEFAULT_OPEN_SOUND =
            Sound.sound(Key.key("minecraft:block.wooden_door.open"), Sound.Source.BLOCK, 2.0F, 1.0F);

    /**
     * Creates a new instance of this record.
     *
     * @param id        the unique id of this door
     * @param opensTo   the unique ids of the rooms to which this door opens to
     * @param costs     the costs, which each represent the cost of opening the door from the room to which their index
     *                  (in opensTo) corresponds
     * @param holograms the holograms to display at each side of the door
     * @param regions   the regions which make up the door
     * @param openSound the sound to play when opening the door
     */
    public DoorInfo {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(opensTo, "opensTo");
        Objects.requireNonNull(costs, "costs");
        Objects.requireNonNull(holograms, "holograms");
        Objects.requireNonNull(regions, "regions");
        Objects.requireNonNull(openSound, "openSound");
    }

    /**
     * Initializes a new DoorInfo with default values (opens to no rooms, has no costs and no holograms).
     *
     * @param id      the id of the door
     * @param regions the regions making up the door
     */
    public DoorInfo(@NotNull Key id, @NotNull List<Bounds3I> regions) {
        this(id, new ArrayList<>(0), new ArrayList<>(0), new ArrayList<>(0), regions, DEFAULT_OPEN_SOUND,
                new ArrayConfigList(0));
    }
}
