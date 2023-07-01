package org.phantazm.zombies.map;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public record PlayerCoinsInfo(@NotNull String transactionMessageFormat,
                              @NotNull String transactionDisplayFormat,
                              @NotNull TextColor gradientFrom,
                              @NotNull TextColor gradientTo,
                              long actionBarDuration) {
}
