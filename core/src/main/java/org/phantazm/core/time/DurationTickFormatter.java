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

@Model("core.tick_formatter.duration")
@Cache
public class DurationTickFormatter implements TickFormatter {

    private final Data data;

    @FactoryMethod
    public DurationTickFormatter(@NotNull Data data) {
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

        TextComponent.Builder builder = Component.text().color(data.color);
        if (hours != 0) {
            builder.append(Component.text(hours));
            if (data.verbose) {
                builder.append(Component.text(" hours"));
            }
            else {
                builder.append(Component.text("h"));
            }
        }
        if (minutes != 0) {
            builder.append(Component.text(minutes));
            if (data.verbose) {
                builder.append(Component.text(" minutes"));
            }
            else {
                builder.append(Component.text("m"));
            }
        }
        if ((hours == 0 && minutes == 0) || seconds != 0) {
            builder.append(Component.text(seconds));
            if (data.verbose) {
                builder.append(Component.text(" seconds"));
            }
            else {
                builder.append(Component.text("s"));
            }
        }

        return builder.build();
    }

    @DataObject
    public record Data(@NotNull TextColor color, boolean verbose, boolean ceil) {
    }
}
