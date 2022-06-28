package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Region3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record DoorInfo(@NotNull Key id,
                       @NotNull List<Key> opensTo,
                       @NotNull List<Integer> costs,
                       @NotNull List<HologramInfo> holograms,
                       @NotNull List<Region3I> regions,
                       @NotNull Sound openSound) {
    public static final Sound DEFAULT_OPEN_SOUND = Sound.sound(Key.key("minecraft:block.wooden_door.open"),
            Sound.Source.BLOCK,
            2.0F,
            1.0F);

    public DoorInfo(@NotNull Key id, @NotNull List<Region3I> regions) {
        this(id, new ArrayList<>(0), new ArrayList<>(0),
                new ArrayList<>(0), regions, DEFAULT_OPEN_SOUND);
    }
}
