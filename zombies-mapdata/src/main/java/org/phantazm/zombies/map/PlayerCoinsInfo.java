package org.phantazm.zombies.map;

import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
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

    @Default("transactionMessageFormat")
    public static @NotNull ConfigElement defaultTransactionMessageFormat() {
        return ConfigPrimitive.of(DEFAULT.transactionMessageFormat);
    }

    @Default("transactionDisplayFormat")
    public static @NotNull ConfigElement defaultTransactionDisplayFormat() {
        return ConfigPrimitive.of(DEFAULT.transactionDisplayFormat);
    }

    @Default("gradientFrom")
    public static @NotNull ConfigElement defaultGradientFrom() {
        return ConfigPrimitive.of("GOLD");
    }

    @Default("gradientTo")
    public static @NotNull ConfigElement defaultGradientTo() {
        return ConfigPrimitive.of("WHITE");
    }

    @Default("actionBarDuration")
    public static @NotNull ConfigElement defaultActionBarDuration() {
        return ConfigPrimitive.of(20);
    }
}
