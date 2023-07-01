package org.phantazm.zombies.map;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public record PlayerCoinsInfo(@NotNull String transactionMessageFormat,
                              @NotNull String transactionDisplayFormat,
                              @NotNull TextColor gradientFrom,
                              @NotNull TextColor gradientTo,
                              long actionBarDuration) {

    public static final PlayerCoinsInfo DEFAULT =
            new PlayerCoinsInfo("<positive:'0#-|1#+'><change> coins<displays_present:' (<displays:, >)'>", "<display>",
                    NamedTextColor.GOLD, NamedTextColor.WHITE, 20);

}
