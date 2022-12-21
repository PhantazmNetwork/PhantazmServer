package org.phantazm.core.time;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class AnalogTickFormatter implements TickFormatter {

    private final TextColor digitColor;

    private final TextColor separatorColor;

    private boolean ceil;

    public AnalogTickFormatter(@NotNull TextColor digitColor, @NotNull TextColor separatorColor, boolean ceil) {
        this.digitColor = Objects.requireNonNull(digitColor, "digitColor");
        this.separatorColor = Objects.requireNonNull(separatorColor, "separatorColor");
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

        Component separator = Component.text(":", separatorColor);
        TextComponent.Builder builder = Component.text().color(digitColor);
        if (hours != 0) {
            builder.append(Component.text(hours)).append(separator);
        }

        builder.append(Component.text(String.format("%02d", minutes))).append(separator);
        builder.append(Component.text(String.format("%02d", seconds)));

        return builder.build();
    }
}
