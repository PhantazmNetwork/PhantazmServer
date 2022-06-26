package com.github.phantazmnetwork.zombies.map;

import com.github.phantazmnetwork.commons.vector.Region3I;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record WindowInfo(@NotNull Region3I frameRegion,
                         @NotNull List<String> repairBlocks,
                         @NotNull Sound repairSound,
                         @NotNull Sound repairAllSound,
                         @NotNull Sound breakSound,
                         @NotNull Sound breakAllSound) {
    public static final Sound DEFAULT_REPAIR_SOUND = Sound.sound(
            Key.key("minecraft:block.wood.place"),
            Sound.Source.PLAYER,
            2.0F,
            1.0F);

    public static final Sound DEFAULT_REPAIR_ALL_SOUND = Sound.sound(
            Key.key("minecraft:block.wooden_trapdoor.close"),
            Sound.Source.PLAYER,
            2.0F,
            1.0F);

    public static final Sound DEFAULT_BREAK_SOUND = Sound.sound(
            Key.key("minecraft:block.wood.break"),
            Sound.Source.HOSTILE,
            2.0F,
            0.8F);

    public static final Sound DEFAULT_BREAK_ALL_SOUND = Sound.sound(
            Key.key("minecraft:entity.zombie.break_wooden_door"),
            Sound.Source.HOSTILE,
            2.0F,
            1.0F);

    public WindowInfo(@NotNull Region3I frameRegion,
                      @NotNull List<String> repairBlocks) {
        this(frameRegion, repairBlocks, DEFAULT_REPAIR_SOUND, DEFAULT_REPAIR_ALL_SOUND, DEFAULT_BREAK_SOUND,
                DEFAULT_BREAK_ALL_SOUND);
    }
}
