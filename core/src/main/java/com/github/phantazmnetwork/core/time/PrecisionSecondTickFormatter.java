package com.github.phantazmnetwork.core.time;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PrecisionSecondTickFormatter implements TickFormatter {

    private final TextColor color;

    private final int decimalPlaces;

    public PrecisionSecondTickFormatter(@NotNull TextColor color, int decimalPlaces) {
        this.color = Objects.requireNonNull(color, "color");
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public @NotNull Component format(long ticks) {
        return Component.text(
                String.format("%." + decimalPlaces + "f", ((double)ticks / MinecraftServer.TICK_PER_SECOND)), color);
    }
}
