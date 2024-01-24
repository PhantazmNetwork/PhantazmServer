package org.phantazm.core.time;

import com.github.steanky.element.core.annotation.Cache;
import com.github.steanky.element.core.annotation.DataObject;
import com.github.steanky.element.core.annotation.FactoryMethod;
import com.github.steanky.element.core.annotation.Model;
import com.github.steanky.ethylene.core.ConfigElement;
import com.github.steanky.ethylene.core.ConfigPrimitive;
import com.github.steanky.ethylene.mapper.annotation.Default;
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

        if (data.showTicks) {
            String leftoverTicks = Long.toString(((ticks % 20) * 50) / 10);
            builder.append('.');
            builder.append(leftoverTicks);
            builder.append("0".repeat(2 - leftoverTicks.length()));
        }

        return builder.toString();
    }

    @DataObject
    public record Data(boolean ceil,
        boolean showTicks) {
        public Data(boolean ceil) {
            this(ceil, false);
        }

        @Default("showTicks")
        public static @NotNull ConfigElement defaultShowTicks() {
            return ConfigPrimitive.FALSE;
        }
    }
}
