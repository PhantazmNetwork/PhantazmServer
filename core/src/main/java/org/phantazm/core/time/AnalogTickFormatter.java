package org.phantazm.core.time;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Model("core.tick_formatter.analog")
@Cache
public class AnalogTickFormatter implements TickFormatter {
    private final Data data;

    @FactoryMethod
    public AnalogTickFormatter(@NotNull Data data) {
        this.data = Objects.requireNonNull(data);
    }

    @Override
    public @NotNull String format(long ticks) {
        long elapsedSeconds;
        if (data.ceil) {
            elapsedSeconds = (long) Math.ceil((double) ticks / MinecraftServer.TICK_PER_SECOND);
        } else {
            elapsedSeconds = ticks / MinecraftServer.TICK_PER_SECOND;
        }
        long hours = elapsedSeconds / 3600;
        long minutes = (elapsedSeconds % 3600) / 60;
        long seconds = elapsedSeconds % 60;

        String separator = ":";
        StringBuilder builder = new StringBuilder();
        if (hours != 0) {
            builder.append(hours).append(separator);
        }

        builder.append(String.format("%02d", minutes)).append(separator);
        builder.append(String.format("%02d", seconds));

        return builder.toString();
    }

    @DataObject
    public record Data(boolean ceil) {

    }
}
