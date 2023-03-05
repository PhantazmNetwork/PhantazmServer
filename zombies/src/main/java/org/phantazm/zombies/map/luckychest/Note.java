package org.phantazm.zombies.map.luckychest;

import net.kyori.adventure.sound.Sound;
import org.jetbrains.annotations.NotNull;

public record Note(@NotNull Sound sound, int delay) {
}
