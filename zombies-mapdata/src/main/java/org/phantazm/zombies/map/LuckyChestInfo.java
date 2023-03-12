package org.phantazm.zombies.map;

import com.github.steanky.vector.Vec3I;
import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

public record LuckyChestInfo(@NotNull Vec3I location, @NotNull Key song) {

}
