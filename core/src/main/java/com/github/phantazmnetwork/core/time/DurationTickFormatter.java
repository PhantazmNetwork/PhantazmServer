package com.github.phantazmnetwork.core.time;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DurationTickFormatter implements TickFormatter {

    private final TextColor color;

    private final boolean ceil;

    public DurationTickFormatter(@NotNull TextColor color, boolean ceil) {
        this.color = Objects.requireNonNull(color, "color");
        this.ceil = ceil;
    }

    @Override
    public @NotNull Component format(long ticks) {
        long elapsedSeconds;
        if (ceil) {
            elapsedSeconds = (long)Math.ceil((double)ticks / MinecraftServer.TICK_PER_SECOND);
        }
        else {
            elapsedSeconds = ticks / MinecraftServer.TICK_PER_SECOND;
        }
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        TextComponent.Builder builder = Component.text().color(color);
        if (hours != 0) {
            builder.append(Component.text(hours)).append(Component.text("h"));
        }
        if (minutes != 0) {
            builder.append(Component.text(minutes)).append(Component.text("m"));
        }
        builder.append(Component.text(seconds)).append(Component.text("s"));

        return builder.build();
    }
}