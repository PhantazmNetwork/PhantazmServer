package org.phantazm.core.time;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("core.tick_formatter.analog")
@Cache
public class AnalogTickFormatter implements TickFormatter {
    private final Data data;

    @FactoryMethod
    public AnalogTickFormatter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data, "data");
    }

    @Override
    public @NotNull Component format(long ticks) {
        long elapsedSeconds;
        if (data.ceil) {
            elapsedSeconds = (long)Math.ceil((double)ticks / MinecraftServer.TICK_PER_SECOND);
        }
        else {
            elapsedSeconds = ticks / MinecraftServer.TICK_PER_SECOND;
        }
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        Component separator = Component.text(":", data.separatorColor);
        TextComponent.Builder builder = Component.text().color(data.digitColor);
        if (hours != 0) {
            builder.append(Component.text(hours)).append(separator);
        }

        builder.append(Component.text(String.format("%02d", minutes))).append(separator);
        builder.append(Component.text(String.format("%02d", seconds)));

        return builder.build();
    }

    @DataObject
    public record Data(@NotNull TextColor digitColor, @NotNull TextColor separatorColor, boolean ceil) {

    }
}
