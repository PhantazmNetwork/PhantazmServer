package com.github.phantazmnetwork.zombies.map;

import com.github.steanky.ethylene.core.collection.ArrayConfigList;
import com.github.steanky.ethylene.core.collection.ConfigList;
import com.github.steanky.vector.Bounds3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Defines a window.
 */
public record WindowInfo(@NotNull Bounds3I frameRegion,
                         @NotNull List<String> repairBlocks,
                         @NotNull Sound repairSound,
                         @NotNull Sound repairAllSound,
                         @NotNull Sound breakSound,
                         @NotNull Sound breakAllSound,
                         @NotNull ConfigList repairActions,
                         @NotNull ConfigList breakActions) {
    /**
     * The default "repair" sound used when a single block is repaired.
     */
    public static final Sound DEFAULT_REPAIR_SOUND =
            Sound.sound(Key.key("minecraft:block.wood.place"), Sound.Source.BLOCK, 2.0F, 1.0F);

    /**
     * The default "repair all" sound used when all blocks in the window are repaired.
     */
    public static final Sound DEFAULT_REPAIR_ALL_SOUND =
            Sound.sound(Key.key("minecraft:block.wooden_trapdoor.close"), Sound.Source.BLOCK, 2.0F, 1.0F);

    /**
     * The default "break" sound used when breaking a single block.
     */
    public static final Sound DEFAULT_BREAK_SOUND =
            Sound.sound(Key.key("minecraft:block.wood.break"), Sound.Source.HOSTILE, 2.0F, 0.8F);

    /**
     * The default "break all" sound used when all blocks in the window are broken.
     */
    public static final Sound DEFAULT_BREAK_ALL_SOUND =
            Sound.sound(Key.key("minecraft:entity.zombie.break_wooden_door"), Sound.Source.HOSTILE, 2.0F, 1.0F);

    /**
     * Creates a new instance of this record.
     *
     * @param frameRegion    the region representing the repairable part of the window
     * @param repairBlocks   the blocks used to repair the window when it is broken
     * @param repairSound    the sound played when a single block is repaired
     * @param repairAllSound the sound played when the window is fully repaired
     * @param breakSound     the sound played when one block is broken
     * @param breakAllSound  the sound played when all blocks are broken
     */
    public WindowInfo {
        Objects.requireNonNull(frameRegion, "frameRegion");
        Objects.requireNonNull(repairBlocks, "repairBlocks");
        Objects.requireNonNull(repairSound, "repairSound");
        Objects.requireNonNull(repairAllSound, "repairAllSound");
        Objects.requireNonNull(breakSound, "breakSound");
        Objects.requireNonNull(breakAllSound, "breakAllSound");
        Objects.requireNonNull(repairActions, "repairActions");
        Objects.requireNonNull(breakActions, "breakActions");
    }

    /**
     * Constructs a new instance of this record, using all the default sounds.
     *
     * @param frameRegion  the region representing the repairable part of the window
     * @param repairBlocks the blocks used to repair the window when it is broken
     */
    public WindowInfo(@NotNull Bounds3I frameRegion, @NotNull List<String> repairBlocks) {
        this(frameRegion, repairBlocks, DEFAULT_REPAIR_SOUND, DEFAULT_REPAIR_ALL_SOUND, DEFAULT_BREAK_SOUND,
                DEFAULT_BREAK_ALL_SOUND, new ArrayConfigList(0), new ArrayConfigList(0));
    }
}
